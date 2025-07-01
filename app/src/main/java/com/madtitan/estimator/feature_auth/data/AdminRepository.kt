package com.madtitan.estimator.feature_auth.data

import com.google.firebase.firestore.snapshots
import com.madtitan.estimator.core.data.FirestoreDataSource
import com.madtitan.estimator.core.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) {
    suspend fun approveUser(userId: String) {
        firestoreDataSource.getDocument("users", userId).update("role", "user").await()
    }

    suspend fun makeAdmin(userId: String) {
        firestoreDataSource.getDocument("users", userId).update("role", "admin").await()
    }

    fun getPendingUsers(): Flow<List<User>> = flow {
        val snapshot = firestoreDataSource.getCollection("users")
            .whereEqualTo("role", "pending")
            .snapshots()

        snapshot.collect { querySnapshot ->
            val users = querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
            emit(users)
        }
    }
}