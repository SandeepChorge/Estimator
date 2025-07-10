package com.madtitan.estimator

import com.madtitan.estimator.feature_auth.CategoryRepositoryImpl
import com.madtitan.estimator.feature_budget.CategoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CategoryRepoModule {
    @Binds
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
}
