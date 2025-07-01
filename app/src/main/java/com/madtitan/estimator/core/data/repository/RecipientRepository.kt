package com.madtitan.estimator.core.data.repository

import com.madtitan.estimator.core.data.FirestoreDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecipientRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) {
    suspend fun getRecipients(eventId: String) =
        firestoreDataSource.getCollection("events/$eventId/recipients").get().await()

    suspend fun addRecipient(eventId: String, recipientId: String, recipientData: Map<String, Any>) =
        firestoreDataSource.setDocument("events/$eventId/recipients", recipientId, recipientData).await()
}