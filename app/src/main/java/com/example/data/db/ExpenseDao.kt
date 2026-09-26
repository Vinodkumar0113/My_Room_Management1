package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE paymentSource = 'POOL'")
    fun getTotalPoolDebitsFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE isRoomExpense = 1")
    fun getTotalRoomExpensesFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE isRoomExpense = 0")
    fun getTotalPersonalExpensesFlow(): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE paidByMemberId = :memberId ORDER BY timestamp DESC")
    fun getExpensesByMember(memberId: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int
}
