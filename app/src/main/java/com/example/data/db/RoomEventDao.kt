package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RoomEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomEventDao {
    @Query("SELECT * FROM room_events ORDER BY dueDate ASC, id ASC")
    fun getAllEvents(): Flow<List<RoomEvent>>

    @Query("SELECT * FROM room_events WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getEventsBetweenDates(startDate: Long, endDate: Long): Flow<List<RoomEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: RoomEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<RoomEvent>)

    @Update
    suspend fun updateEvent(event: RoomEvent)

    @Delete
    suspend fun deleteEvent(event: RoomEvent)

    @Query("DELETE FROM room_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("UPDATE room_events SET isCompleted = :isCompleted, status = :status, completedAt = :completedAt, completedByMemberName = :completedByName WHERE id = :id")
    suspend fun updateCompletion(id: Long, isCompleted: Boolean, status: String, completedAt: Long?, completedByName: String?)
}
