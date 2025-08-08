package com.madtitan.estimator.feature_auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madtitan.estimator.core.data.FirestoreDataSource
import com.madtitan.estimator.core.domain.Payment
import com.madtitan.estimator.feature_budget.PercentValue
import com.madtitan.estimator.feature_budget.ReportRepository
import com.madtitan.estimator.feature_budget.ReportStats
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirestoreDataSource,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore // Inject native Firestore
) : ReportRepository {

  /*  override suspend fun getReport(userId: String, from: Timestamp, to: Timestamp): ReportStats {
        // 1️⃣ Fetch all payments in the date range
        val payments = firestore.getCollection("payments")
            .whereEqualTo("accountId", userId)
            .whereGreaterThanOrEqualTo("timestamp", from)
            .whereLessThanOrEqualTo("timestamp", to)
            .get().await()
            .toObjects(Payment::class.java)

        // 2️⃣ Fetch categories
        val categoryDocs = db.collection("categories").get().await()
        val categoryMap = categoryDocs.associate { doc ->
            doc.id to (doc.getString("name") ?: "Unknown")
        }

        // 3️⃣ Fetch subcategories
        val subCategoryMap = mutableMapOf<String, String>()
        for (catDoc in categoryDocs) {
            val subcats = db.collection("categories")
                .document(catDoc.id)
                .collection("subcategories")
                .get().await()

            for (subDoc in subcats.documents) {
                subCategoryMap["${catDoc.id} > ${subDoc.id}"] =
                    "${catDoc.getString("name") ?: "Unknown"} > ${subDoc.getString("name") ?: "Unknown"}"
            }
        }

        // 4️⃣ Replace IDs in payments
        val paymentsWithNames = payments.map { payment ->
            val categoryName = categoryMap[payment.category] ?: "Unknown"
            val subCategoryName = payment.subCategory?.let {
                subCategoryMap["${payment.category} > $it"] ?: "$categoryName > Unknown"
            } ?: "$categoryName > -"
            payment.copy(
                category = categoryName,
                subCategory = subCategoryName.substringAfter("> ").trim()
            )
        }

        // 5️⃣ Group and calculate percentages
        val income = paymentsWithNames.filter { it.type == "income" }
        val expense = paymentsWithNames.filter { it.type == "expense" }

        return ReportStats(
            totalIncome = income.sumOf { it.amount },
            totalExpense = expense.sumOf { it.amount },
            totalRemaining = income.sumOf { it.amount } - expense.sumOf { it.amount },
            incomeByCategory = toPercentMap(income.groupBy { it.category }, income.sumOf { it.amount }),
            expenseByCategory = toPercentMap(expense.groupBy { it.category }, expense.sumOf { it.amount }),
            incomeBySubCategory = toPercentMap(income.groupBy { "${it.category} > ${it.subCategory}" }, income.sumOf { it.amount }),
            expenseBySubCategory = toPercentMap(expense.groupBy { "${it.category} > ${it.subCategory}" }, expense.sumOf { it.amount })
        )
    }

    private fun toPercentMap(grouped: Map<String, List<Payment>>, total: Double): Map<String, PercentValue> {
        return grouped.mapValues { (_, list) ->
            val sum = list.sumOf { it.amount }
            val percent = if (total > 0) (sum / total * 100) else 0.0
            PercentValue(
                amount = String.format("%.2f", sum).toDouble(),
                percent = String.format("%.2f", percent).toDouble()
            )
        }
    }*/

    override suspend fun getReport(userId: String, from: Timestamp, to: Timestamp): ReportStats {
        // 1️⃣ Fetch all payments in the date range
        val payments = firestore.getCollection("payments")
            .whereEqualTo("accountId", userId)
            .whereGreaterThanOrEqualTo("timestamp", from)
            .whereLessThanOrEqualTo("timestamp", to)
            .get().await()
            .toObjects(Payment::class.java)

        // 2️⃣ Fetch categories
        val categoryDocs = db.collection("categories").get().await()
        val categoryMap = categoryDocs.associate { doc ->
            doc.id to (doc.getString("name") ?: "Unknown")
        }

        // 3️⃣ Fetch subcategories
        val subCategoryMap = mutableMapOf<String, String>()
        for (catDoc in categoryDocs) {
            val subcats = db.collection("categories")
                .document(catDoc.id)
                .collection("subcategories")
                .get().await()

            for (subDoc in subcats.documents) {
                subCategoryMap["${catDoc.id} > ${subDoc.id}"] =
                    subDoc.getString("name") ?: "Unknown"
            }
        }

        // 4️⃣ Replace IDs in payments
        val paymentsWithNames = payments.map { payment ->
            val categoryName = categoryMap[payment.category] ?: "Unknown"
            val subCategoryName = payment.subCategory?.let {
                subCategoryMap["${payment.category} > $it"] ?: "Unknown"
            } ?: "-"
            payment.copy(
                category = categoryName,
                subCategory = subCategoryName
            )
        }

        // 5️⃣ Separate income & expense
        val income = paymentsWithNames.filter { it.type == "income" }
        val expense = paymentsWithNames.filter { it.type == "expense" }

        // 6️⃣ Build subcategory data grouped by main category
        val expenseBySubCategoryGrouped = expense
            .groupBy { it.category } // group by main category
            .mapValues { (_, list) ->
                toPercentMap(
                    list.groupBy { it.subCategory },
                    list.sumOf { it.amount }
                )
            }

        val incomeBySubCategoryGrouped = income
            .groupBy { it.category }
            .mapValues { (_, list) ->
                toPercentMap(
                    list.groupBy { it.subCategory },
                    list.sumOf { it.amount }
                )
            }

        // 7️⃣ Return
        return ReportStats(
            totalIncome = income.sumOf { it.amount },
            totalExpense = expense.sumOf { it.amount },
            totalRemaining = income.sumOf { it.amount } - expense.sumOf { it.amount },
            incomeByCategory = toPercentMap(income.groupBy { it.category }, income.sumOf { it.amount }),
            expenseByCategory = toPercentMap(expense.groupBy { it.category }, expense.sumOf { it.amount }),
            incomeBySubCategoryGrouped = incomeBySubCategoryGrouped, // 🔹 New grouped structure
            expenseBySubCategoryGrouped = expenseBySubCategoryGrouped // 🔹 New grouped structure
        )
    }

    private fun toPercentMap(grouped: Map<String, List<Payment>>, total: Double): Map<String, PercentValue> {
        return grouped.mapValues { (_, list) ->
            val sum = list.sumOf { it.amount }
            val percent = if (total > 0) (sum / total * 100) else 0.0
            PercentValue(
                amount = String.format("%.2f", sum).toDouble(),
                percent = String.format("%.2f", percent).toDouble()
            )
        }
    }

}