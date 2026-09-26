package com.example.data.repository

import com.example.data.db.ExpenseDao
import com.example.data.db.PoolDepositDao
import com.example.data.db.RoommateDao
import com.example.data.model.Expense
import com.example.data.model.PoolDeposit
import com.example.data.model.Roommate
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val roommateDao: RoommateDao,
    private val poolDepositDao: PoolDepositDao,
    private val expenseDao: ExpenseDao
) {
    val allRoommates: Flow<List<Roommate>> = roommateDao.getAllRoommates()
    val allDeposits: Flow<List<PoolDeposit>> = poolDepositDao.getAllDeposits()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

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

    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesBetweenDates(startDate, endDate)
    }

    fun getDepositsBetweenDates(startDate: Long, endDate: Long): Flow<List<PoolDeposit>> {
        return poolDepositDao.getDepositsBetweenDates(startDate, endDate)
    }
}
