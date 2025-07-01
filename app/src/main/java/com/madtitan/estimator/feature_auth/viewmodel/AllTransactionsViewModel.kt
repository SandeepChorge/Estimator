package com.madtitan.estimator.feature_auth.viewmodel

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.madtitan.estimator.FilterState
import com.madtitan.estimator.core.domain.Payment
import com.madtitan.estimator.formatRelativeDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

private const val PAGE_SIZE = 25L

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Inject or set this from your login state
    private val accountId = FirebaseAuth.getInstance().currentUser?.uid
    // Current filters
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Backing list of loaded payments
    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()
    val groupedPayments: StateFlow<Map<String, List<Payment>>> =
        payments.map { list ->
            list.distinctBy { it.id } // Optional defensive measure
                .groupBy { payment ->
                    formatRelativeDate(payment.timestamp.toDate())
                }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyMap()
        )

    // For pagination
    private var lastSnapshot: DocumentSnapshot? = null
    private var loading = false
    private var initialized = false

    init {
        if (!initialized) {
            initialized = true
            filterState
                .drop(1)
                .distinctUntilChangedBy { it.modes to (it.startDate to it.endDate) }
                .onEach {
                    resetAndLoad()
                }
                .launchIn(viewModelScope)
        }
    }

    private fun resetAndLoad() {
        lastSnapshot = null
        _payments.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() {
        if (loading || accountId == null) return
        loading = true

        viewModelScope.launch {
            try {
                Log.d("AllTransactionsViewModel", "accountId $accountId");
                var q: Query = firestore.collection("payments")
                    .whereEqualTo("accountId", accountId) // ← required for security rule
                    .orderBy("timestamp", Query.Direction.DESCENDING)

                // Apply mode filters
                val modes = filterState.value.modes
                if (modes.isNotEmpty()) {
                    q = q.whereIn("paymentMode", modes.toList().take(10))
                }
                // Apply date range
                filterState.value.startDate?.let { q = q.whereGreaterThanOrEqualTo("timestamp", normalizeStartOfDay(it.toDate())) }
                filterState.value.endDate?.let { q = q.whereLessThanOrEqualTo("timestamp", normalizeEndOfDay(it.toDate())) }

                // Pagination
                lastSnapshot?.let { q = q.startAfter(it) }
                q = q.limit(PAGE_SIZE)

                val snap = q.get().await()
                val fetched = snap.toObjects(Payment::class.java)
                lastSnapshot = snap.documents.lastOrNull()

                val existingIds = _payments.value.map { it.id }.toSet()
                val newItems = fetched.filter { it.id !in existingIds }
                _payments.value += newItems
            } catch (e: Exception) {
                // TODO: handle errors
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    fun updateModes(selected: Set<String>) {
        _filterState.update { it.copy(modes = selected) }
    }
    fun updateDateRange(start: Timestamp?, end: Timestamp?) {
        _filterState.update { it.copy(
            startDate = start?.let { Timestamp(normalizeStartOfDay(it.toDate())) },
            endDate = end?.let { Timestamp(normalizeEndOfDay(it.toDate())) }
        ) }
    }
    fun clearFilter() = _filterState.value.let {
        _filterState.value = FilterState()
    }

    private fun normalizeStartOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun normalizeEndOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }
}
