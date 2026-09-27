package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.Expense
import com.example.ui.components.AddDepositDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddMemberDialog
import com.example.ui.components.ApproveExpenseDialog
import com.example.ui.components.FilterDateDialog
import com.example.ui.components.RejectExpenseDialog
import com.example.ui.components.SettleExpenseDialog
import com.example.ui.components.SwitchUserBottomSheet
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoommatesScreen
import com.example.ui.screens.ScanPayScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.RoomieTheme
import com.example.ui.viewmodel.ExpenseViewModel

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : Screen("home", "Wallet", Icons.Filled.Home, Icons.Outlined.Home)
    object Events : Screen("events", "Room Life", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable)
    object Roommates : Screen("roommates", "Roommates", Icons.Filled.Group, Icons.Outlined.Group)
    object ScanPay : Screen("scan_pay", "Scan QR", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner)
    object Expenses : Screen("expenses", "Transactions", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Stats : Screen("stats", "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoomieTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: ExpenseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showAddDepositSheet by remember { mutableStateOf(false) }
    var showAddMemberSheet by remember { mutableStateOf(false) }
    var showDateFilterSheet by remember { mutableStateOf(false) }
    var showSwitchUserSheet by remember { mutableStateOf(false) }

    var expenseToSettle by remember { mutableStateOf<Expense?>(null) }
    var expenseToApprove by remember { mutableStateOf<Expense?>(null) }
    var expenseToReject by remember { mutableStateOf<Expense?>(null) }

    val expenseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val depositSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val memberSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFilterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val switchUserSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settleSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val approveSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rejectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (!uiState.isAuthenticated) {
        AuthScreen(
            roommates = uiState.roommates,
            errorMessage = uiState.authErrorMessage,
            onLoginWithPin = { member, pin -> viewModel.loginWithPin(member, pin) },
            onLoginWithIdentifier = { id, pin -> viewModel.loginWithEmailOrPhone(id, pin) },
            onRegister = { name, email, phone, pin, role, colorHex, isAdmin, deposit, autoPay ->
                viewModel.registerNewUser(name, email, phone, pin, role, colorHex, isAdmin, deposit, autoPay)
            },
            onClearError = { viewModel.clearAuthError() }
        )
        return
    }

    val navItems = listOf(
        Screen.Home,
        Screen.Events,
        Screen.Expenses,
        Screen.Roommates,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onOpenAddExpense = { showAddExpenseSheet = true },
                    onOpenAddDeposit = { showAddDepositSheet = true },
                    onOpenAddMember = { showAddMemberSheet = true },
                    onOpenDateFilter = { showDateFilterSheet = true },
                    onOpenSwitchUser = { showSwitchUserSheet = true },
                    onOpenQrScanner = { navController.navigate(Screen.ScanPay.route) },
                    onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                    onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                    onToggleEventCompletion = { viewModel.toggleEventCompletion(it) },
                    onSettleExpense = { expenseToSettle = it },
                    onApproveExpense = { expenseToApprove = it },
                    onRejectExpense = { expenseToReject = it },
                    onIgnoreExpense = { viewModel.ignoreExpense(it) },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
            }

            composable(Screen.Events.route) {
                EventsScreen(
                    uiState = uiState,
                    onAddEvent = { title, cat, memberId, memberName, colorHex, dueDate, dayOfWeek, isBill, billAmount, recurrence, notes ->
                        viewModel.addRoomEvent(title, cat, memberId, memberName, colorHex, dueDate, dayOfWeek, isBill, billAmount, recurrence, notes)
                    },
                    onToggleCompletion = { viewModel.toggleEventCompletion(it) },
                    onUpdateStatus = { event, newStatus -> viewModel.updateEventStatus(event, newStatus) },
                    onDeleteEvent = { viewModel.deleteRoomEvent(it) },
                    onPayUtilityBill = { event, memberId, memberName, source ->
                        viewModel.payUtilityBillDirectly(event, memberId, memberName, source)
                    }
                )
            }

            composable(Screen.Roommates.route) {
                RoommatesScreen(
                    uiState = uiState,
                    onOpenAddMember = { showAddMemberSheet = true },
                    onToggleAdmin = { viewModel.toggleAdminRole(it) },
                    onDeleteMember = { viewModel.deleteRoommate(it) }
                )
            }

            composable(Screen.ScanPay.route) {
                ScanPayScreen(
                    uiState = uiState,
                    onPayViaQr = { merchant, amount, memberId, memberName, category, isRoom, notes, qrData ->
                        viewModel.payViaQrCode(merchant, amount, memberId, memberName, category, isRoom, notes, qrData)
                        navController.navigate(Screen.Expenses.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onVerifyPin = { viewModel.verifyWalletPin(it) },
                    onVerifyBiometric = { viewModel.verifyBiometric() }
                )
            }

            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    uiState = uiState,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onExpenseTypeFilterChange = { viewModel.setExpenseTypeFilter(it) },
                    onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                    onOpenAddExpense = { showAddExpenseSheet = true },
                    onOpenDateFilter = { showDateFilterSheet = true },
                    onSettleExpense = { expenseToSettle = it },
                    onApproveExpense = { expenseToApprove = it },
                    onRejectExpense = { expenseToReject = it },
                    onIgnoreExpense = { viewModel.ignoreExpense(it) },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
            }

            composable(Screen.Stats.route) {
                StatsScreen(
                    uiState = uiState,
                    onOpenDateFilter = { showDateFilterSheet = true }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    uiState = uiState,
                    onLogout = { viewModel.logout() },
                    onSwitchUser = { showSwitchUserSheet = true },
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onUpdateAutoPay = { enabled, amount, day, source ->
                        viewModel.updateAutoPaySettings(enabled, amount, day, source)
                    },
                    onTriggerAutoPayNow = { amount, source ->
                        viewModel.triggerAutoPayDepositNow(amount, source)
                    },
                    onAddLinkedAccount = { viewModel.addLinkedAccount(it) },
                    onRemoveLinkedAccount = { viewModel.removeLinkedAccount(it) },
                    onUpdateLanguage = { viewModel.updateLanguage(it) },
                    onUpdatePin = { viewModel.changeWalletPin(it) },
                    onUpdateSecurityPolicies = { thresh, limit, bio ->
                        viewModel.updateSecuritySettings(thresh, limit, bio)
                    }
                )
            }
        }

        // Modals & Bottom Sheets
        if (showAddExpenseSheet) {
            AddExpenseDialog(
                roommates = uiState.roommates,
                currentMember = uiState.currentMember,
                sheetState = expenseSheetState,
                onDismiss = { showAddExpenseSheet = false },
                onAddExpense = { title, amount, paidById, paidByName, cat, isRoom, source, notes, method, recipient ->
                    viewModel.addExpense(title, amount, paidById, paidByName, cat, isRoom, source, notes, method, recipient)
                }
            )
        }

        if (showAddDepositSheet) {
            AddDepositDialog(
                roommates = uiState.roommates,
                sheetState = depositSheetState,
                onDismiss = { showAddDepositSheet = false },
                onAddDeposit = { memberId, memberName, amount, note ->
                    viewModel.addDeposit(memberId, memberName, amount, note)
                }
            )
        }

        if (showAddMemberSheet) {
            AddMemberDialog(
                sheetState = memberSheetState,
                onDismiss = { showAddMemberSheet = false },
                onAddMember = { name, role, colorHex, note, isAdmin ->
                    viewModel.addRoommate(name, role, colorHex, note, isAdmin)
                }
            )
        }

        if (showDateFilterSheet) {
            FilterDateDialog(
                currentPeriod = uiState.timePeriod,
                customStart = uiState.customStartDate,
                customEnd = uiState.customEndDate,
                sheetState = dateFilterSheetState,
                onDismiss = { showDateFilterSheet = false },
                onSelectPeriod = { viewModel.setTimePeriod(it) },
                onSelectCustomRange = { start, end -> viewModel.setCustomDateRange(start, end) }
            )
        }

        if (showSwitchUserSheet) {
            SwitchUserBottomSheet(
                roommates = uiState.roommates,
                currentMember = uiState.currentMember,
                sheetState = switchUserSheetState,
                onDismiss = { showSwitchUserSheet = false },
                onSelectUser = { selectedUser ->
                    viewModel.switchLoggedInUser(selectedUser)
                    showSwitchUserSheet = false
                }
            )
        }

        expenseToSettle?.let { exp ->
            SettleExpenseDialog(
                expense = exp,
                sheetState = settleSheetState,
                onDismiss = { expenseToSettle = null },
                onConfirmSettle = { expense, note ->
                    viewModel.settlePersonalExpense(expense, note)
                    expenseToSettle = null
                }
            )
        }

        expenseToApprove?.let { exp ->
            ApproveExpenseDialog(
                expense = exp,
                sheetState = approveSheetState,
                onDismiss = { expenseToApprove = null },
                onConfirmApprove = { expense, category, note ->
                    viewModel.approveExpenseAsRoom(expense, category, note)
                    expenseToApprove = null
                }
            )
        }

        expenseToReject?.let { exp ->
            RejectExpenseDialog(
                expense = exp,
                sheetState = rejectSheetState,
                onDismiss = { expenseToReject = null },
                onConfirmReject = { expense, reason ->
                    viewModel.rejectUnapprovedExpense(expense, reason)
                    expenseToReject = null
                }
            )
        }
    }
}
