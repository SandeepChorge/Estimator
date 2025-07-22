package com.madtitan.estimator.feature_auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.madtitan.estimator.core.data.repository.TagGeneratorRepository
import com.madtitan.estimator.core.domain.Payment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val tagRepository: TagGeneratorRepository
) : ViewModel() {

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
            onGenerated(tag)
        }
    }
}
