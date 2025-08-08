package com.madtitan.estimator.feature_budget

import com.google.firebase.Timestamp

interface ReportRepository {
    suspend fun getReport(
        userId: String,
        from: Timestamp,
        to: Timestamp
    ): ReportStats
}
