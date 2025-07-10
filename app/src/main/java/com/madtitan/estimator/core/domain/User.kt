package com.madtitan.estimator.core.domain

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val createdAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
)

data class Account(
    val id: String = "",
    val name: String = "",
    val totalLimit: Double = 0.0,
    val spentAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val ownerId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
)

data class Payee(
    val id: String = "",
    val name: String = "",
    val contact: String = "",
    val category: String = "", // e.g., "Groceries", "Utilities", "Freelancer"
    val totalAmount: Double = 0.0,
    val advancePaid: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val accountId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
)

data class Payment(
    val id: String = "",
    val paymentMode: String = "",  // e.g., UPI, Cash, Credit Card
    val type: String = "expense", // "income", "expense", "borrow", "lent"
    val notes: String = "",
    val payeeId: String = "",  // formerly recipientId
    val amount: Double = 0.0,
    val accountId: String = "",  // formerly eventId
    val timestamp: Timestamp = Timestamp.now(),
    val category: String = "",            // e.g., "Food"
    val subCategory: String = "",         // e.g., "Dinner"
    val counterpartyName: String = "",    // used only for borrow/lent
    val tag: String = "",                 // Auto-generated tag per grouped transaction  #BR01
    val linkedToTag: String? = null,       // For repayments (points to #BR01 or #LN02),
    val isDeleted: Boolean = false
)

data class Reminder(
    val payeeId: String = "",
    val status: String = "", // e.g., pending, done
    val createdBy: String = "",
    val amountDue: Double = 0.0,
    val accountId: String = "",
    val dueDate: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
)

data class Category(
    val id: String = "",
    val name: String = "", // e.g., "Food"
    val createdBy: String = "", // uid
    val createdAt: Timestamp = Timestamp.now(),
    @field:PropertyName("deleted")
    val isDeleted: Boolean = false
)

data class SubCategory(
    val id: String = "",
    val name: String = "", // e.g., "Dinner"
    val parentCategoryId: String = "", // foreign key to Category
    val createdBy: String = "", // uid
    val createdAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
)

data class CategoryWithSubCategories(
    val category: Category,
    val subCategories: List<SubCategory>
)
