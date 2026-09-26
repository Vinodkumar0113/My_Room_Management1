package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roommates")
data class Roommate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val pin: String = "1234",
    val avatarColorHex: String = "#4F46E5",
    val role: String = "Roommate",
    val phoneOrNote: String = "",
    val isAdmin: Boolean = false,
    val autoPayEnabled: Boolean = false,
    val autoPayAmount: Double = 500.0,
    val autoPayDay: Int = 1,
    val autoPaySource: String = "Google Pay UPI",
    val linkedAccounts: String = "HDFC Bank (••••4321)|Google Pay UPI",
    val preferredLanguage: String = "English"
)
