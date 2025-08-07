package com.madtitan.estimator.core.data.repository


import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madtitan.estimator.core.data.FirestoreDataSource
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class ExportRepository @Inject constructor(
    private val context: Context,
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth
) {

    suspend fun exportUserDataToJson(): Uri? {
        val userId = auth.currentUser?.uid ?: return null

        val paymentsSnapshot = firestoreDataSource.getCollection("payments")
            .whereEqualTo("accountId", userId)
            .get().await()

        val categoriesSnapshot = firestoreDataSource.getCollection("categories")
            .whereEqualTo("createdBy", userId)
            .get().await()

        val paymentsJson = JSONArray()
        for (doc in paymentsSnapshot) paymentsJson.put(JSONObject(doc.data))

        val categoriesJson = JSONArray()
        val subcategoriesJson = JSONArray()

        for (cat in categoriesSnapshot) {
            categoriesJson.put(JSONObject(cat.data))

            val subSnap = firestoreDataSource.getCollection("categories/${cat.id}/subcategories")
                .whereEqualTo("createdBy", userId)
                .get().await()

            for (sub in subSnap) {
                subcategoriesJson.put(JSONObject(sub.data))
            }
        }

        val fullJson = JSONObject().apply {
            put("payments", paymentsJson)
            put("categories", categoriesJson)
            put("subcategories", subcategoriesJson)
        }

        return saveJsonToFile(fullJson)
    }

    private fun saveJsonToFile(json: JSONObject): Uri {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val file = File(downloadsDir, "export_${System.currentTimeMillis()}.json")
        file.writeText(json.toString(2))

        // Use FileProvider if you want to share, or return file URI directly
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

}