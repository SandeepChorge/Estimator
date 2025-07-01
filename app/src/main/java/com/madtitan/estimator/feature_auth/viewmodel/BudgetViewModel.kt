package com.madtitan.estimator.feature_auth.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.madtitan.estimator.core.domain.Payment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    fun addPayment(payment: Payment, onComplete: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("payments")
            .document(payment.id)
            .set(payment.copy(accountId = userId)) // temporarily using eventId to track owner
            .addOnSuccessListener { onComplete() }
    }

    fun fetchRecentPayments(limit: Long = 5): Flow<List<Payment>> = callbackFlow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("payments")
            .whereEqualTo("accountId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, _ ->
                val result = snapshot?.toObjects(Payment::class.java) ?: emptyList()
                trySend(result)
            }

        awaitClose { listener.remove() }
    }
}
