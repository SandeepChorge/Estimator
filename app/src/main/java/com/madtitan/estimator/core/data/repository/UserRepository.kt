package com.madtitan.estimator.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.madtitan.estimator.core.data.FirestoreDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth
) {
    suspend fun getUser(userId: String) =
        firestoreDataSource.getDocument("users", userId).get().await()

    suspend fun addUser(userId: String, userData: Map<String, Any>) =
        firestoreDataSource.setDocument("users", userId, userData).await()

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}