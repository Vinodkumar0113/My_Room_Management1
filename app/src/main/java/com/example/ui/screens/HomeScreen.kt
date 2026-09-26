package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.ui.components.ExpenseItemCard
import com.example.ui.components.MiniStatCard
import com.example.ui.components.WalletSummaryCard
import com.example.ui.theme.EmeraldPositive
import com.example.ui.theme.RoseDebit
import com.example.ui.viewmodel.FilterTimePeriod
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: UiState,
    onOpenAddExpense: () -> Unit,
    onOpenAddDeposit: () -> Unit,
    onOpenAddMember: () -> Unit,
    onOpenDateFilter: () -> Unit,
    onOpenSwitchUser: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onSettleExpense: (Expense) -> Unit,
    onApproveExpense: (Expense) -> Unit,
    onRejectExpense: (Expense) -> Unit,
    onIgnoreExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    var dashboardFilterTab by remember { mutableStateOf("ALL") } // ALL, ACTION_NEEDED, ROOM, PERSONAL
    var showAllDashboardTransactions by remember { mutableStateOf(true) }

    val defaultAvatarColor = MaterialTheme.colorScheme.primary
    val userAvatarColor = remember(uiState.currentMember?.avatarColorHex, defaultAvatarColor) {
        val hex = uiState.currentMember?.avatarColorHex
        if (!hex.isNullOrBlank()) {
            try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) {
                defaultAvatarColor
            }
        } else {
            defaultAvatarColor
        }
    }

    val currentUserSpends = uiState.expenses
        .filter { it.paidByMemberId == uiState.currentMember?.id }
        .sumOf { it.amount }

    val displayedTransactions = when (dashboardFilterTab) {
        "ACTION_NEEDED" -> uiState.filteredExpenses.filter { !it.isRoomExpense && !it.isSettled && it.approvalStatus != "IGNORED" }
        "ROOM" -> uiState.filteredExpenses.filter { it.isRoomExpense }
        "PERSONAL" -> uiState.filteredExpenses.filter { !it.isRoomExpense }
        else -> uiState.filteredExpenses
    }

    val transactionsToShow = if (showAllDashboardTransactions) {
        displayedTransactions
    } else {
        displayedTransactions.take(6)
    }

    val userSubtitle = remember(uiState.currentMember, uiState.roommates.size) {
        val member = uiState.currentMember
        when {
            member == null -> "${uiState.roommates.size} roommates in pool"
            member.email.isNotBlank() -> member.email
            member.phoneOrNote.isNotBlank() -> member.phoneOrNote
            else -> "${uiState.roommates.size} roommates in pool"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. DASHBOARD HEADER & CURRENT USER NAME
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_user_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(userAvatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.currentMember?.name?.take(1)?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.currentMember?.name ?: "Roommate",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("dashboard_user_name")
                                )
                                if (uiState.isAdminLoggedIn) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = "👑 Admin",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFD97706),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = userSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Switch User & Filter Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenSwitchUser,
                            modifier = Modifier.testTag("dashboard_switch_user_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch User",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenDateFilter,
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("date_filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Date Filter",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (uiState.timePeriod) {
                                    FilterTimePeriod.THIS_MONTH -> "This Month"
                                    FilterTimePeriod.LAST_MONTH -> "Last Month"
                                    FilterTimePeriod.ALL_TIME -> "All Time"
                                    FilterTimePeriod.CUSTOM -> "Custom"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. WALLET BALANCE & TOTAL SPENT SUMMARY
        // ==========================================
        item {
            WalletSummaryCard(
                loggedInUser = uiState.currentMember,
                totalDeposited = uiState.totalPoolDeposited,
                totalDebited = uiState.totalPoolDebited,
                remainingBalance = uiState.remainingPoolBalance,
                onSwitchUserClick = onOpenSwitchUser
            )
        }

        // Quick Stats Row: Total Spent Breakdown
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStatCard(
                    title = "Total Room Spent",
                    amount = uiState.totalRoomExpenses,
                    icon = Icons.Default.Home,
                    iconBgColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("total_room_spent_card")
                )

                MiniStatCard(
                    title = "Your Total Spent",
                    amount = currentUserSpends,
                    icon = Icons.Default.Person,
                    iconBgColor = Color(0xFF8B5CF6),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("user_total_spent_card")
                )
            }
        }

        // ==========================================
        // 3. DIRECT ACTION BUTTONS (Deposit & Spend)
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenAddDeposit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPositive,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("direct_add_deposit_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Add", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add to Wallet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = onOpenAddExpense,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("direct_add_spend_button")
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Spend/Pay", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Spend / Pay", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Action Alert: Unsettled Spends / Admin Review Callout
        if (uiState.unsettledPersonalSpends.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dashboardFilterTab = "ACTION_NEEDED" }
                        .testTag("action_needed_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (uiState.isAdminLoggedIn) Color(0xFFFEF3C7) else Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isAdminLoggedIn) Icons.Default.Shield else Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${uiState.unsettledPersonalSpends.size} Transaction(s) Need Action",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Use 'Approve', 'Ignore', or 'Settle' on the transactions below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB45309)
                            )
                        }
                        FilledTonalButton(
                            onClick = { dashboardFilterTab = "ACTION_NEEDED" },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. DIRECT QR SCANNER BANNER
        // ==========================================
        item {
            Card(
                onClick = onOpenQrScanner,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("direct_qr_scanner_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0284C7).copy(alpha = 0.12f),
                                    Color(0xFF4F46E5).copy(alpha = 0.12f)
                                )
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF0284C7), Color(0xFF4F46E5))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "QR Scanner",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Direct QR Scanner",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Scan merchant QR codes & debit wallet directly",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onOpenQrScanner,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("scan_now_button")
                        ) {
                            Text("Scan QR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. TRANSACTIONS SECTION & ACTION CONTROLS
        // ==========================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "All Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Manage approvals, settlements, and ignores",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { showAllDashboardTransactions = !showAllDashboardTransactions },
                            modifier = Modifier.testTag("toggle_show_all_transactions")
                        ) {
                            Text(
                                if (showAllDashboardTransactions) "Show Top 6" else "Show All (${displayedTransactions.size})"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Tabs: All, Action Needed, Room Expenses, Personal Spends
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = dashboardFilterTab == "ALL",
                            onClick = { dashboardFilterTab = "ALL" },
                            label = { Text("All (${uiState.filteredExpenses.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("dashboard_filter_all")
                        )
                    }

                    val actionNeededCount = uiState.filteredExpenses.count { !it.isRoomExpense && !it.isSettled && it.approvalStatus != "IGNORED" }
                    if (actionNeededCount > 0) {
                        item {
                            FilterChip(
                                selected = dashboardFilterTab == "ACTION_NEEDED",
                                onClick = { dashboardFilterTab = "ACTION_NEEDED" },
                                label = { Text("⚡ Needs Action ($actionNeededCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFEF3C7),
                                    selectedLabelColor = Color(0xFF92400E)
                                ),
                                modifier = Modifier.testTag("dashboard_filter_action_needed")
                            )
                        }
                    }

                    item {
                        FilterChip(
                            selected = dashboardFilterTab == "ROOM",
                            onClick = { dashboardFilterTab = "ROOM" },
                            label = { Text("Room Shared") },
                            modifier = Modifier.testTag("dashboard_filter_room")
                        )
                    }

                    item {
                        FilterChip(
                            selected = dashboardFilterTab == "PERSONAL",
                            onClick = { dashboardFilterTab = "PERSONAL" },
                            label = { Text("Personal Spends") },
                            modifier = Modifier.testTag("dashboard_filter_personal")
                        )
                    }
                }
            }
        }

        // ==========================================
        // 6. TRANSACTION LIST ITEMS WITH ACTION BUTTONS
        // ==========================================
        if (transactionsToShow.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "No Expenses",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (dashboardFilterTab == "ACTION_NEEDED") "All transactions are reviewed & settled!" else "No transactions found in this view",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Spend via QR Scanner or Phone Pay to debit expenses directly from your shared wallet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(transactionsToShow, key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    isAdmin = uiState.isAdminLoggedIn,
                    onDeleteClick = onDeleteExpense,
                    onSettleClick = onSettleExpense,
                    onApproveClick = onApproveExpense,
                    onRejectClick = onRejectExpense,
                    onIgnoreClick = onIgnoreExpense
                )
            }
        }

        // Bottom space for scrolling comfort
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

