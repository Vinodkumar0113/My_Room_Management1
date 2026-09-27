package com.example.data.repository

import com.example.data.db.ExpenseDao
import com.example.data.db.PoolDepositDao
import com.example.data.db.RoommateDao
import com.example.data.db.RoomEventDao
import com.example.data.model.Expense
import com.example.data.model.PoolDeposit
import com.example.data.model.Roommate
import com.example.data.model.RoomEvent
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val roommateDao: RoommateDao,
    private val poolDepositDao: PoolDepositDao,
    private val expenseDao: ExpenseDao,
    private val roomEventDao: RoomEventDao
) {
    val allRoommates: Flow<List<Roommate>> = roommateDao.getAllRoommates()
    val allDeposits: Flow<List<PoolDeposit>> = poolDepositDao.getAllDeposits()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allEvents: Flow<List<RoomEvent>> = roomEventDao.getAllEvents()

    val totalDeposits: Flow<Double?> = poolDepositDao.getTotalDepositsFlow()
    val totalPoolDebits: Flow<Double?> = expenseDao.getTotalPoolDebitsFlow()
    val totalRoomExpenses: Flow<Double?> = expenseDao.getTotalRoomExpensesFlow()
    val totalPersonalExpenses: Flow<Double?> = expenseDao.getTotalPersonalExpensesFlow()

    suspend fun insertRoommate(roommate: Roommate): Long = roommateDao.insertRoommate(roommate)
    suspend fun updateRoommate(roommate: Roommate) = roommateDao.updateRoommate(roommate)
    suspend fun deleteRoommate(roommate: Roommate) = roommateDao.deleteRoommate(roommate)

    suspend fun insertDeposit(deposit: PoolDeposit): Long = poolDepositDao.insertDeposit(deposit)
    suspend fun deleteDeposit(deposit: PoolDeposit) = poolDepositDao.deleteDeposit(deposit)

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun insertEvent(event: RoomEvent): Long = roomEventDao.insertEvent(event)
    suspend fun updateEvent(event: RoomEvent) = roomEventDao.updateEvent(event)
    suspend fun deleteEvent(event: RoomEvent) = roomEventDao.deleteEvent(event)
    suspend fun deleteEventById(id: Long) = roomEventDao.deleteEventById(id)
    suspend fun updateEventCompletion(id: Long, isCompleted: Boolean, status: String, completedAt: Long?, completedByName: String?) =
        roomEventDao.updateCompletion(id, isCompleted, status, completedAt, completedByName)

    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesBetweenDates(startDate, endDate)
    }

    fun getDepositsBetweenDates(startDate: Long, endDate: Long): Flow<List<PoolDeposit>> {
        return poolDepositDao.getDepositsBetweenDates(startDate, endDate)
    }

    fun getEventsBetweenDates(startDate: Long, endDate: Long): Flow<List<RoomEvent>> {
        return roomEventDao.getEventsBetweenDates(startDate, endDate)
    }
}
