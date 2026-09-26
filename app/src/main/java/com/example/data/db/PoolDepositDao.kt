package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PoolDeposit
import kotlinx.coroutines.flow.Flow

@Dao
interface PoolDepositDao {
    @Query("SELECT * FROM pool_deposits ORDER BY timestamp DESC")
    fun getAllDeposits(): Flow<List<PoolDeposit>>

    @Query("SELECT SUM(amount) FROM pool_deposits")
    fun getTotalDepositsFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM pool_deposits WHERE memberId = :memberId")
    fun getMemberDepositsFlow(memberId: Long): Flow<Double?>

    @Query("SELECT * FROM pool_deposits WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getDepositsBetweenDates(startDate: Long, endDate: Long): Flow<List<PoolDeposit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: PoolDeposit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposits(deposits: List<PoolDeposit>)

    @Delete
    suspend fun deleteDeposit(deposit: PoolDeposit)
}
