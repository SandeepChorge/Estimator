package com.madtitan.estimator.core.data
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getCollection(path: String) = firestore.collection(path)
    fun getDocument(path: String, documentId: String) = firestore.collection(path).document(documentId)
    fun setDocument(path: String, documentId: String, data: Any) = firestore.collection(path).document(documentId).set(data)
    fun deleteDocument(path: String, documentId: String) = firestore.collection(path).document(documentId).delete()
}