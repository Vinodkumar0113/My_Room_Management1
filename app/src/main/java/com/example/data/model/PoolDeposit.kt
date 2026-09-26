package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pool_deposits")
data class PoolDeposit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val memberName: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "Pool deposit"
)
