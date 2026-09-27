package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_events")
data class RoomEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "CLEANING", "COOKING", "UTILITY_BILL", "MAINTENANCE", "GROCERY_RUN"
    val assignedMemberId: Long? = null,
    val assignedMemberName: String = "Unassigned",
    val assignedMemberColorHex: String = "#4F46E5",
    val dueDate: Long, // timestamp for due date
    val dayOfWeek: Int = 1, // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val status: String = "TODO", // "TODO", "IN_PROGRESS", "COMPLETED"
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val completedByMemberName: String? = null,
    val isBill: Boolean = false,
    val billAmount: Double? = null,
    val recurrence: String = "WEEKLY", // "ONCE", "DAILY", "WEEKLY", "MONTHLY"
    val notes: String = ""
)
