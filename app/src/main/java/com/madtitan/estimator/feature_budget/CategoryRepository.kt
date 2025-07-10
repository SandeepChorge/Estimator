package com.madtitan.estimator.feature_budget

import com.madtitan.estimator.core.domain.Category
import com.madtitan.estimator.core.domain.SubCategory

interface CategoryRepository {
    suspend fun getCategoriesByUser(uid: String): List<Category>
    suspend fun getSubCategoriesByParent(categoryId: String): List<SubCategory>
    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)

    suspend fun addSubCategory(sub: SubCategory)
    suspend fun updateSubCategory(sub: SubCategory)
    suspend fun deleteSubCategory(subCategoryId: String)
}
