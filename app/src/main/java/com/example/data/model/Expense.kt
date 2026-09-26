package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val paidByMemberId: Long,
    val paidByMemberName: String,
    val category: String, // e.g. "Room Rent", "Groceries", "Electricity", "Water", "Wifi", "Personal", "Other"
    val isRoomExpense: Boolean = true, // true = Room/Shared expense, false = Personal expense
    val paymentSource: String = "POOL", // "POOL" = debited from wallet pool, "OUT_OF_POCKET" = paid individually
    val paymentMethod: String = "DIRECT", // "QR_SCAN", "PHONE_NUMBER", "DIRECT"
    val recipientDetail: String = "", // e.g. "UPI: store@upi", "Phone: +91 9876543210"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val splitAmongMemberIds: String = "ALL", // Comma-separated IDs or "ALL"
    val isSettled: Boolean = false, // true if personal wallet spend was reimbursed
    val approvalStatus: String = "ACTIVE", // "ACTIVE", "PERSONAL_PENDING", "APPROVED_ROOM", "SETTLED", "REJECTED", "IGNORED"
    val approvedByAdmin: String? = null
)

