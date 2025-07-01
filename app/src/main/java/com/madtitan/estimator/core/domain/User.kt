package com.madtitan.estimator.core.domain

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val createdAt: Timestamp = Timestamp.now()
)

data class Account(
    val id: String = "",
    val name: String = "",
    val totalLimit: Double = 0.0,
    val spentAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val ownerId: String = "",
    val createdAt: Timestamp = Timestamp.now()
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
    val createdAt: Timestamp = Timestamp.now()
)

data class Payment(
    val id: String = "",
    val paymentMode: String = "",  // e.g., UPI, Cash, Credit Card
    val notes: String = "",
    val payeeId: String = "",  // formerly recipientId
    val amount: Double = 0.0,
    val accountId: String = "",  // formerly eventId
    val timestamp: Timestamp = Timestamp.now()
)

data class Reminder(
    val payeeId: String = "",
    val status: String = "", // e.g., pending, done
    val createdBy: String = "",
    val amountDue: Double = 0.0,
    val accountId: String = "",
    val dueDate: Timestamp = Timestamp.now()
)

