package com.madtitan.estimator.feature_auth

import com.google.firebase.firestore.FirebaseFirestore
import com.madtitan.estimator.core.domain.Category
import com.madtitan.estimator.core.domain.SubCategory
import com.madtitan.estimator.feature_budget.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    override suspend fun getCategoriesByUser(uid: String): List<Category> = withContext(Dispatchers.IO) {
        firestore.collection("categories")
            .whereEqualTo("createdBy", uid)
            .get()
            .await()
            .toObjects(Category::class.java)
    }

    override suspend fun getSubCategoriesByParent(categoryId: String): List<SubCategory> = withContext(Dispatchers.IO) {
        firestore.collection("categories")
            .document(categoryId)
            .collection("subcategories")
            .get()
            .await()
            .toObjects(SubCategory::class.java)
    }

    override suspend fun addCategory(category: Category) {
        firestore.collection("categories")
            .document(category.id)
            .set(category)
            .await()
    }

    override suspend fun updateCategory(category: Category) {
        firestore.collection("categories")
            .document(category.id)
            .set(category)
            .await()
    }

    override suspend fun deleteCategory(categoryId: String) {
        firestore.collection("categories")
            .document(categoryId)
            .update("isDeleted", true)
            .await()
    }

    override suspend fun addSubCategory(sub: SubCategory) {
        firestore.collection("categories")
            .document(sub.parentCategoryId)
            .collection("subcategories")
            .document(sub.id)
            .set(sub)
            .await()
    }

    override suspend fun updateSubCategory(sub: SubCategory) {
        firestore.collection("categories")
            .document(sub.parentCategoryId)
            .collection("subcategories")
            .document(sub.id)
            .set(sub)
            .await()
    }

    override suspend fun deleteSubCategory(subCategoryId: String) {
        // This requires parentCategoryId context
        // So we should pass SubCategory, not just ID
        throw UnsupportedOperationException("Use updateSubCategory with isDeleted=true instead.")
    }
}
