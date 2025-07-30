package com.madtitan.estimator.feature_auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madtitan.estimator.core.data.repository.TagGeneratorRepository
import com.madtitan.estimator.core.domain.Payment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val tagRepository: TagGeneratorRepository
) : ViewModel() {

    private val _borrowedOrLentTags = MutableStateFlow<List<String>>(emptyList())
    val borrowedOrLentTags: StateFlow<List<String>> = _borrowedOrLentTags


    fun getPaymentById(paymentId: String): Flow<Payment?> = callbackFlow {
        val ref = firestore.collection("payments").document(paymentId)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Payment::class.java))
        }
        awaitClose { listener.remove() }
    }

    fun generateNewTag(type: String, onGenerated: (String) -> Unit) {
        viewModelScope.launch {
            val tag = tagRepository.generateTagForType(type)
            Log.e("Generated tag: $tag"," for type: $type")
            onGenerated(tag)
        }
    }


    fun fetchBorrowedOrLentTags() {
        viewModelScope.launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                val snapshot = firestore.collection("payments")
                    .whereEqualTo("accountId", uid)
                    .whereIn("type", listOf("borrow", "lent"))
                    .get()
                    .await()

                val tags = snapshot.documents.mapNotNull {
                    it.getString("tag")?.takeIf { tag -> tag.isNotBlank() }
                }.distinct()

                _borrowedOrLentTags.value = tags
            } catch (e: Exception) {
                e.printStackTrace()
                _borrowedOrLentTags.value = emptyList()
            }
        }
    }

    fun getLinkedPaymentsForTag(tag: String): Flow<List<Payment>> = callbackFlow {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val query = firestore.collection("payments")
            .whereEqualTo("accountId", uid)
            .whereEqualTo("linkedToTag", tag)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val payments = snapshot?.toObjects(Payment::class.java) ?: emptyList()
            trySend(payments)
        }

        awaitClose { listener.remove() }
    }



}
