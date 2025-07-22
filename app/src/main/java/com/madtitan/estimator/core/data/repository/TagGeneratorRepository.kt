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
        val tagPrefix = when (type.lowercase()) {
            "expense" -> "EX"
            "income" -> "IN"
            "borrow" -> "BR"
            "lent" -> "LN"
            else -> throw IllegalArgumentException("Invalid type: $type")
        }

        val tagCounterDocRef = firestore
            .collection("tag_counters")
            .document(userId)

        return suspendCoroutine { continuation ->
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(tagCounterDocRef)

                val currentCount = snapshot.getLong(type) ?: 0L
                val nextCount = currentCount + 1

                transaction.set(
                    tagCounterDocRef,
                    mapOf(type to nextCount),
                    SetOptions.merge()
                )

                val tag = "#$tagPrefix%02d".format(nextCount)
                continuation.resume(tag)

            }.addOnFailureListener { e ->
                // ✅ Only resume if not already resumed
                if (continuation.context[Job]?.isActive == true) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

}