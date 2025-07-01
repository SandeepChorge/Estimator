package com.madtitan.estimator.core.data.repository

import com.madtitan.estimator.core.data.FirestoreDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) {
    suspend fun getPayments(eventId: String, recipientId: String) =
        firestoreDataSource.getCollection("events/$eventId/recipients/$recipientId/payments").get().await()

    suspend fun addPayment(eventId: String, recipientId: String, paymentId: String, paymentData: Map<String, Any>) =
        firestoreDataSource.setDocument("events/$eventId/recipients/$recipientId/payments", paymentId, paymentData).await()
}