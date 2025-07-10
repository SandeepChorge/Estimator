package com.madtitan.estimator.feature_auth.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.madtitan.estimator.core.domain.CategoryWithSubCategories
import com.madtitan.estimator.feature_budget.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.madtitan.estimator.core.domain.Category
import com.madtitan.estimator.core.domain.SubCategory
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.runtime.State

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repo: CategoryRepository,
    private val auth: FirebaseAuth // or your AuthRepository
) : ViewModel() {

    private val _categoryList = mutableStateListOf<CategoryWithSubCategories>()
    val categoryList: List<CategoryWithSubCategories> = _categoryList

    private val _expandedIds = mutableStateOf(setOf<String>())
    val expandedIds: State<Set<String>> = _expandedIds

    private val currentUserId get() = auth.currentUser?.uid ?: ""



    init {
        viewModelScope.launch {
            loadCategoriesWithSubs()
        }
    }

    fun getCategoryNameById(categoryId: String): String {
        return categoryList.firstOrNull { it.category.id == categoryId }?.category?.name.orEmpty()
    }

    fun getSubCategoryNameById(categoryId: String, subCategoryId: String): String {
        return categoryList
            .firstOrNull { it.category.id == categoryId }
            ?.subCategories?.firstOrNull { it.id == subCategoryId }
            ?.name.orEmpty()
    }


    fun toggleExpanded(id: String) {
        _expandedIds.value = _expandedIds.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    private suspend fun loadCategoriesWithSubs() {
        val cats = repo.getCategoriesByUser(currentUserId).filter { !it.isDeleted }
        val result = mutableListOf<CategoryWithSubCategories>()

        for (cat in cats) {
            val subs = repo.getSubCategoriesByParent(cat.id).filter { !it.isDeleted }
            result.add(CategoryWithSubCategories(cat, subs))
        }

        _categoryList.clear()
        _categoryList.addAll(result)
    }

    fun refresh() {
        viewModelScope.launch {
            loadCategoriesWithSubs()
        }
    }

    fun addCategory(name: String) {
        val newCat = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            createdBy = currentUserId
        )
        viewModelScope.launch {
            repo.addCategory(newCat)
            refresh()
        }
    }

    fun updateCategory(updated: Category) {
        viewModelScope.launch {
            repo.updateCategory(updated)
            refresh()
        }
    }

    fun deleteCategory(cat: Category) {
        viewModelScope.launch {
            repo.updateCategory(cat.copy(isDeleted = true)) // soft delete
            refresh()
        }
    }

    fun addSubCategory(name: String, parentCategoryId: String) {
        val newSub = SubCategory(
            id = UUID.randomUUID().toString(),
            name = name,
            parentCategoryId = parentCategoryId,
            createdBy = currentUserId
        )
        viewModelScope.launch {
            repo.addSubCategory(newSub)
            refresh()
        }
    }

    fun updateSubCategory(sub: SubCategory) {
        viewModelScope.launch {
            repo.updateSubCategory(sub)
            refresh()
        }
    }

    fun deleteSubCategory(sub: SubCategory) {
        viewModelScope.launch {
            repo.updateSubCategory(sub.copy(isDeleted = true))
            refresh()
        }
    }
}
