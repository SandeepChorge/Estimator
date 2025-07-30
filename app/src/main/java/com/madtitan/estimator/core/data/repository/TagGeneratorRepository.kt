package com.madtitan.estimator.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TagGeneratorRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val userId get() = auth.currentUser?.uid.orEmpty()


    suspend fun generateTagForType(type: String): String {
        val tagCounterDocRef = firestore
            .collection("tag_counters")
            .document(userId)

        return suspendCoroutine { continuation ->
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(tagCounterDocRef)

                // Initialize doc if it doesn't exist
                val data = snapshot.data ?: mapOf("global" to 0L)
                val currentCount = (data["global"] as? Long) ?: 0L
                val nextCount = currentCount + 1

                // ✅ Write only after all reads
                transaction.set(
                    tagCounterDocRef,
                    mapOf("global" to nextCount),
                    SetOptions.merge()
                )

                val tag = "#TX%02d".format(nextCount)
                continuation.resume(tag)
            }.addOnFailureListener { e ->
                if (continuation.context[Job]?.isActive == true) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }




}