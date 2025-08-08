package com.madtitan.estimator.feature_budget

data class ReportStats(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val incomeByCategory: Map<String, PercentValue> = emptyMap(),
    val expenseByCategory: Map<String, PercentValue> = emptyMap(),
    val incomeBySubCategory: Map<String, PercentValue> = emptyMap(),
    val expenseBySubCategory: Map<String, PercentValue> = emptyMap(),
    // ✅ New fields for grouped subcategories
    val incomeBySubCategoryGrouped: Map<String, Map<String, PercentValue>> = emptyMap(),
    val expenseBySubCategoryGrouped: Map<String, Map<String, PercentValue>> = emptyMap()
)

data class PercentValue(
    val amount: Double,
    val percent: Double
)
