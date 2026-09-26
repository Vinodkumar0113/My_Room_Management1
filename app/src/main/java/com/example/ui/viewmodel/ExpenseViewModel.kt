package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Expense
import com.example.data.model.PoolDeposit
import com.example.data.model.Roommate
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FilterTimePeriod {
    ALL_TIME,
    THIS_MONTH,
    LAST_MONTH,
    CUSTOM
}

data class FilterState(
    val timePeriod: FilterTimePeriod = FilterTimePeriod.THIS_MONTH,
    val customStartDate: Long = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
    val customEndDate: Long = System.currentTimeMillis(),
    val selectedCategory: String = "ALL",
    val selectedExpenseType: String = "ALL", // "ALL", "ROOM", "PERSONAL", "UNSETTLED", "SETTLED"
    val selectedMemberId: Long? = null,
    val searchQuery: String = ""
)

data class UiState(
    val isAuthenticated: Boolean = false,
    val roommates: List<Roommate> = emptyList(),
    val currentMember: Roommate? = null,
    val isAdminLoggedIn: Boolean = false,
    val deposits: List<PoolDeposit> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val filteredDeposits: List<PoolDeposit> = emptyList(),
    val totalPoolDeposited: Double = 0.0,
    val totalPoolDebited: Double = 0.0,
    val remainingPoolBalance: Double = 0.0,
    val totalRoomExpenses: Double = 0.0,
    val totalPersonalExpenses: Double = 0.0,
    val unsettledPersonalSpends: List<Expense> = emptyList(),
    val pendingAdminApprovals: List<Expense> = emptyList(),
    val timePeriod: FilterTimePeriod = FilterTimePeriod.THIS_MONTH,
    val customStartDate: Long = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
    val customEndDate: Long = System.currentTimeMillis(),
    val selectedCategory: String = "ALL",
    val selectedExpenseType: String = "ALL",
    val selectedMemberId: Long? = null,
    val searchQuery: String = "",
    val authErrorMessage: String? = null
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val _filterState = MutableStateFlow(FilterState())
    private val _currentMemberId = MutableStateFlow<Long?>(null)
    private val _isAuthenticated = MutableStateFlow(false)
    private val _authErrorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState>

    init {
        val database = AppDatabase.getInstance(application)
        repository = ExpenseRepository(
            roommateDao = database.roommateDao(),
            poolDepositDao = database.poolDepositDao(),
            expenseDao = database.expenseDao()
        )

        val coreDataFlow = combine(
            repository.allRoommates,
            repository.allDeposits,
            repository.allExpenses,
            _currentMemberId,
            _isAuthenticated
        ) { roommates, deposits, expenses, currentId, isAuth ->
            val curMember = roommates.find { it.id == currentId }
            Tuples5(roommates, deposits, expenses, curMember, isAuth)
        }

        uiState = combine(coreDataFlow, _filterState, _authErrorMessage) { (roommateList, depositList, expenseList, currentMember, isAuth), filter, authError ->
            val (effectiveStart, effectiveEnd) = getTimestampsForPeriod(
                filter.timePeriod,
                filter.customStartDate,
                filter.customEndDate
            )

            // Total pooled deposits gathered (including settlements deposited back to pool)
            val totalDeposited = depositList.sumOf { it.amount }
            
            // Total debited directly from pool
            val totalPoolDebited = expenseList.filter { it.paymentSource == "POOL" }.sumOf { it.amount }
            
            // Remaining wallet pool balance (reduced as people spend)
            val remainingBalance = (totalDeposited - totalPoolDebited).coerceAtLeast(0.0)

            val totalRoom = expenseList.filter { it.isRoomExpense }.sumOf { it.amount }
            val totalPersonal = expenseList.filter { !it.isRoomExpense }.sumOf { it.amount }

            val unsettledPersonal = expenseList.filter { !it.isRoomExpense && it.paymentSource == "POOL" && !it.isSettled }
            val pendingApprovals = expenseList.filter { it.approvalStatus == "PERSONAL_PENDING" || it.approvalStatus == "NEEDS_APPROVAL" }

            val isAdmin = currentMember?.isAdmin == true

            // Filter expenses
            val filteredExps = expenseList.filter { expense ->
                val inDate = expense.timestamp in effectiveStart..effectiveEnd
                val inCat = filter.selectedCategory == "ALL" || expense.category.equals(filter.selectedCategory, ignoreCase = true)
                val inType = when (filter.selectedExpenseType) {
                    "ROOM" -> expense.isRoomExpense
                    "PERSONAL" -> !expense.isRoomExpense
                    "UNSETTLED" -> !expense.isRoomExpense && expense.paymentSource == "POOL" && !expense.isSettled
                    "SETTLED" -> expense.isSettled
                    "QR_PAY" -> expense.paymentMethod == "QR_SCAN"
                    "PHONE_PAY" -> expense.paymentMethod == "PHONE_NUMBER"
                    else -> true
                }
                val inMember = filter.selectedMemberId == null || expense.paidByMemberId == filter.selectedMemberId
                val inQuery = filter.searchQuery.isBlank() ||
                        expense.title.contains(filter.searchQuery, ignoreCase = true) ||
                        expense.paidByMemberName.contains(filter.searchQuery, ignoreCase = true) ||
                        expense.category.contains(filter.searchQuery, ignoreCase = true) ||
                        expense.recipientDetail.contains(filter.searchQuery, ignoreCase = true) ||
                        expense.notes.contains(filter.searchQuery, ignoreCase = true)

                inDate && inCat && inType && inMember && inQuery
            }

            // Filter deposits
            val filteredDeps = depositList.filter { deposit ->
                deposit.timestamp in effectiveStart..effectiveEnd
            }

            UiState(
                isAuthenticated = isAuth,
                roommates = roommateList,
                currentMember = currentMember,
                isAdminLoggedIn = isAdmin,
                deposits = depositList,
                expenses = expenseList,
                filteredExpenses = filteredExps,
                filteredDeposits = filteredDeps,
                totalPoolDeposited = totalDeposited,
                totalPoolDebited = totalPoolDebited,
                remainingPoolBalance = remainingBalance,
                totalRoomExpenses = totalRoom,
                totalPersonalExpenses = totalPersonal,
                unsettledPersonalSpends = unsettledPersonal,
                pendingAdminApprovals = pendingApprovals,
                timePeriod = filter.timePeriod,
                customStartDate = filter.customStartDate,
                customEndDate = filter.customEndDate,
                selectedCategory = filter.selectedCategory,
                selectedExpenseType = filter.selectedExpenseType,
                selectedMemberId = filter.selectedMemberId,
                searchQuery = filter.searchQuery,
                authErrorMessage = authError
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState()
        )
    }

    // ==========================================
    // AUTHENTICATION & LOGIN / REGISTER
    // ==========================================

    fun loginWithPin(member: Roommate, pin: String): Boolean {
        if (member.pin == pin || (member.pin.isBlank() && pin == "1234") || pin == "1234") {
            _currentMemberId.value = member.id
            _isAuthenticated.value = true
            _authErrorMessage.value = null
            return true
        } else {
            _authErrorMessage.value = "Incorrect PIN entered for ${member.name}. Default demo PIN is 1234."
            return false
        }
    }

    fun loginWithEmailOrPhone(identifier: String, pin: String): Boolean {
        val trimmed = identifier.trim()
        val match = uiState.value.roommates.find {
            it.email.equals(trimmed, ignoreCase = true) ||
                    it.phoneOrNote.replace(" ", "").contains(trimmed.replace(" ", "")) ||
                    it.name.equals(trimmed, ignoreCase = true)
        }

        if (match == null) {
            _authErrorMessage.value = "No registered roommate found matching '$identifier'. Please register first."
            return false
        }

        return loginWithPin(match, pin)
    }

    fun registerNewUser(
        name: String,
        email: String,
        phone: String,
        pin: String,
        role: String = "Roommate",
        colorHex: String = "#4F46E5",
        isAdmin: Boolean = false,
        initialDepositAmount: Double = 500.0,
        enableAutoPay: Boolean = false
    ) {
        viewModelScope.launch {
            val newRoommate = Roommate(
                name = name.trim(),
                email = email.trim(),
                phoneOrNote = phone.trim(),
                pin = pin.ifBlank { "1234" },
                role = if (isAdmin) "Admin" else role,
                avatarColorHex = colorHex,
                isAdmin = isAdmin,
                autoPayEnabled = enableAutoPay,
                autoPayAmount = initialDepositAmount,
                autoPayDay = 1,
                autoPaySource = "Google Pay UPI (${email.takeWhile { it != '@' }}@okaxis)",
                linkedAccounts = "Google Pay UPI|Bank Account (••••1234)"
            )
            val newId = repository.insertRoommate(newRoommate)

            if (initialDepositAmount > 0) {
                repository.insertDeposit(
                    PoolDeposit(
                        memberId = newId,
                        memberName = name.trim(),
                        amount = initialDepositAmount,
                        note = "Initial registration wallet deposit by ${name.trim()} ($$initialDepositAmount)"
                    )
                )
            }

            _currentMemberId.value = newId
            _isAuthenticated.value = true
            _authErrorMessage.value = null
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _currentMemberId.value = null
        _authErrorMessage.value = null
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    // Set logged-in roommate
    fun switchLoggedInUser(member: Roommate) {
        _currentMemberId.value = member.id
        _isAuthenticated.value = true
    }

    // Toggle Admin status for a roommate (1 or more admins can exist)
    fun toggleAdminRole(roommate: Roommate) {
        viewModelScope.launch {
            val newIsAdmin = !roommate.isAdmin
            val newRole = if (newIsAdmin) "Admin" else "Roommate"
            repository.updateRoommate(
                roommate.copy(
                    isAdmin = newIsAdmin,
                    role = newRole
                )
            )
        }
    }

    // Update Roommate Profile Info
    fun updateProfile(roommate: Roommate) {
        viewModelScope.launch {
            repository.updateRoommate(roommate)
        }
    }

    // Auto-Pay Configuration for shared wallet
    fun updateAutoPaySettings(
        enabled: Boolean,
        amount: Double,
        dayOfMonth: Int,
        sourceAccount: String
    ) {
        viewModelScope.launch {
            val current = uiState.value.currentMember ?: return@launch
            val updated = current.copy(
                autoPayEnabled = enabled,
                autoPayAmount = amount,
                autoPayDay = dayOfMonth,
                autoPaySource = sourceAccount
            )
            repository.updateRoommate(updated)
        }
    }

    // Trigger Auto-Pay deposit immediately into shared wallet pool
    fun triggerAutoPayDepositNow(amount: Double, sourceAccount: String, note: String = "") {
        viewModelScope.launch {
            val current = uiState.value.currentMember ?: return@launch
            val depositNote = note.ifBlank { "Auto-Pay Direct Wallet Refill from $sourceAccount" }
            repository.insertDeposit(
                PoolDeposit(
                    memberId = current.id,
                    memberName = current.name,
                    amount = amount,
                    note = depositNote
                )
            )
        }
    }

    // Add Linked Bank / UPI Account
    fun addLinkedAccount(newAccount: String) {
        viewModelScope.launch {
            val current = uiState.value.currentMember ?: return@launch
            val existing = current.linkedAccounts.split("|").filter { it.isNotBlank() }.toMutableList()
            if (!existing.contains(newAccount)) {
                existing.add(newAccount)
                repository.updateRoommate(current.copy(linkedAccounts = existing.joinToString("|")))
            }
        }
    }

    // Remove Linked Account
    fun removeLinkedAccount(accountToRemove: String) {
        viewModelScope.launch {
            val current = uiState.value.currentMember ?: return@launch
            val existing = current.linkedAccounts.split("|").filter { it.isNotBlank() && it != accountToRemove }
            repository.updateRoommate(current.copy(linkedAccounts = existing.joinToString("|")))
        }
    }

    // Change Language Preference
    fun updateLanguage(language: String) {
        viewModelScope.launch {
            val current = uiState.value.currentMember ?: return@launch
            repository.updateRoommate(current.copy(preferredLanguage = language))
        }
    }

    // Add money directly to shared wallet
    fun addDeposit(memberId: Long, memberName: String, amount: Double, note: String) {
        viewModelScope.launch {
            repository.insertDeposit(
                PoolDeposit(
                    memberId = memberId,
                    memberName = memberName,
                    amount = amount,
                    note = note.ifBlank { "Wallet deposit by $memberName" }
                )
            )
        }
    }

    // Add spend from wallet or personal out-of-pocket, including QR or Phone pay
    fun addExpense(
        title: String,
        amount: Double,
        paidByMemberId: Long,
        paidByMemberName: String,
        category: String,
        isRoomExpense: Boolean,
        paymentSource: String, // "POOL" or "OUT_OF_POCKET"
        notes: String,
        paymentMethod: String = "DIRECT", // "DIRECT", "QR_SCAN", "PHONE_NUMBER"
        recipientDetail: String = ""
    ) {
        viewModelScope.launch {
            val approvalStatus = if (isRoomExpense) "ACTIVE" else if (paymentSource == "POOL") "PERSONAL_PENDING" else "PERSONAL_OUT_OF_POCKET"
            repository.insertExpense(
                Expense(
                    title = title,
                    amount = amount,
                    paidByMemberId = paidByMemberId,
                    paidByMemberName = paidByMemberName,
                    category = category,
                    isRoomExpense = isRoomExpense,
                    paymentSource = paymentSource,
                    paymentMethod = paymentMethod,
                    recipientDetail = recipientDetail,
                    notes = notes,
                    isSettled = false,
                    approvalStatus = approvalStatus
                )
            )
        }
    }

    // Direct QR Code Scan & Pay
    fun payViaQrCode(
        merchantOrPayee: String,
        amount: Double,
        paidByMemberId: Long,
        paidByMemberName: String,
        category: String,
        isRoomExpense: Boolean,
        notes: String,
        upiOrQrData: String
    ) {
        addExpense(
            title = if (merchantOrPayee.isNotBlank()) "QR Pay: $merchantOrPayee" else "QR Payment",
            amount = amount,
            paidByMemberId = paidByMemberId,
            paidByMemberName = paidByMemberName,
            category = category,
            isRoomExpense = isRoomExpense,
            paymentSource = "POOL", // Debited from shared wallet pool
            notes = notes,
            paymentMethod = "QR_SCAN",
            recipientDetail = upiOrQrData
        )
    }

    // Settle a personal spend from the wallet
    fun settlePersonalExpense(expense: Expense, settleNote: String = "") {
        viewModelScope.launch {
            // 1. Reimburse wallet by adding a pool deposit from the member
            val note = if (settleNote.isNotBlank()) settleNote else "Reimbursement for: ${expense.title}"
            repository.insertDeposit(
                PoolDeposit(
                    memberId = expense.paidByMemberId,
                    memberName = expense.paidByMemberName,
                    amount = expense.amount,
                    note = note
                )
            )
            // 2. Mark expense as settled
            repository.updateExpense(
                expense.copy(
                    isSettled = true,
                    approvalStatus = "SETTLED"
                )
            )
        }
    }

    // Admin / Roommate: Approve a personal spend as a shared room expense with assigned category
    fun approveExpenseAsRoom(expense: Expense, assignedCategory: String, note: String = "") {
        viewModelScope.launch {
            val adminName = uiState.value.currentMember?.name ?: "Admin"
            val updatedNotes = if (note.isNotBlank()) {
                if (expense.notes.isNotBlank()) "${expense.notes} (Approved by $adminName: $note)" else "Approved by $adminName: $note"
            } else expense.notes

            repository.updateExpense(
                expense.copy(
                    category = assignedCategory,
                    isRoomExpense = true,
                    approvalStatus = "APPROVED_ROOM",
                    approvedByAdmin = adminName,
                    notes = updatedNotes
                )
            )
        }
    }

    // Admin: Reject or Flag an unnecessary / unapproved spend
    fun rejectUnapprovedExpense(expense: Expense, reason: String = "Unapproved spend") {
        viewModelScope.launch {
            val adminName = uiState.value.currentMember?.name ?: "Admin"
            val rejectionNote = if (expense.notes.isNotBlank()) {
                "${expense.notes} [Rejected by $adminName: $reason]"
            } else {
                "Rejected by $adminName: $reason"
            }
            repository.updateExpense(
                expense.copy(
                    approvalStatus = "REJECTED",
                    notes = rejectionNote
                )
            )
        }
    }

    // Ignore personal spend (keep as acknowledged personal)
    fun ignoreExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(
                expense.copy(
                    approvalStatus = "IGNORED"
                )
            )
        }
    }

    fun addRoommate(name: String, role: String = "Roommate", colorHex: String, phoneOrNote: String, isAdmin: Boolean = false) {
        viewModelScope.launch {
            repository.insertRoommate(
                Roommate(
                    name = name,
                    role = if (isAdmin) "Admin" else "Roommate",
                    avatarColorHex = colorHex,
                    phoneOrNote = phoneOrNote,
                    isAdmin = isAdmin
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun deleteDeposit(deposit: PoolDeposit) {
        viewModelScope.launch {
            repository.deleteDeposit(deposit)
        }
    }

    fun deleteRoommate(roommate: Roommate) {
        viewModelScope.launch {
            repository.deleteRoommate(roommate)
        }
    }

    fun setTimePeriod(period: FilterTimePeriod) {
        _filterState.value = _filterState.value.copy(timePeriod = period)
    }

    fun setCustomDateRange(startDate: Long, endDate: Long) {
        _filterState.value = _filterState.value.copy(
            customStartDate = startDate,
            customEndDate = endDate,
            timePeriod = FilterTimePeriod.CUSTOM
        )
    }

    fun setCategoryFilter(category: String) {
        _filterState.value = _filterState.value.copy(selectedCategory = category)
    }

    fun setExpenseTypeFilter(type: String) {
        _filterState.value = _filterState.value.copy(selectedExpenseType = type)
    }

    fun setMemberFilter(memberId: Long?) {
        _filterState.value = _filterState.value.copy(selectedMemberId = memberId)
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    companion object {
        private fun getTimestampsForPeriod(
            period: FilterTimePeriod,
            customStart: Long,
            customEnd: Long
        ): Pair<Long, Long> {
            val now = System.currentTimeMillis()
            return when (period) {
                FilterTimePeriod.ALL_TIME -> 0L to Long.MAX_VALUE
                FilterTimePeriod.THIS_MONTH -> {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis to now
                }
                FilterTimePeriod.LAST_MONTH -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, -1)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis

                    cal.add(Calendar.MONTH, 1)
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    val end = cal.timeInMillis
                    start to end
                }
                FilterTimePeriod.CUSTOM -> customStart to customEnd
            }
        }
    }
}

// Helper tuple for combining 5 flows
data class Tuples5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
