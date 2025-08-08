package com.madtitan.estimator.feature_auth.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.madtitan.estimator.core.data.repository.UserRepository
import com.madtitan.estimator.feature_budget.ReportRepository
import com.madtitan.estimator.feature_budget.ReportStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repo: ReportRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    var stats by mutableStateOf<ReportStats?>(null)
        private set

    fun loadReport(from: Timestamp, to: Timestamp) {
        viewModelScope.launch {
            val userId = userRepo.getCurrentUserId() ?: return@launch
            stats = repo.getReport(userId, from, to)
        }
    }
}
