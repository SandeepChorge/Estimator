package com.madtitan.estimator.core.data.repository

import com.madtitan.estimator.core.data.FirestoreDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) {
    suspend fun getEventsForUser(userId: String) =
        firestoreDataSource.getCollection("events")
            .whereArrayContains("members", userId).get().await()

    suspend fun createEvent(eventId: String, eventData: Map<String, Any>) =
        firestoreDataSource.setDocument("events", eventId, eventData).await()
}