package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Roommate
import kotlinx.coroutines.flow.Flow

@Dao
interface RoommateDao {
    @Query("SELECT * FROM roommates ORDER BY name ASC")
    fun getAllRoommates(): Flow<List<Roommate>>

    @Query("SELECT * FROM roommates WHERE id = :id")
    suspend fun getRoommateById(id: Long): Roommate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoommate(roommate: Roommate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoommates(roommates: List<Roommate>)

    @Update
    suspend fun updateRoommate(roommate: Roommate)

    @Delete
    suspend fun deleteRoommate(roommate: Roommate)

    @Query("SELECT COUNT(*) FROM roommates")
    suspend fun getRoommateCount(): Int
}
