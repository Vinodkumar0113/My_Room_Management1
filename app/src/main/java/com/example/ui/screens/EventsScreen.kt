package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoomEvent
import com.example.ui.components.AddEventDialog
import com.example.ui.components.PayBillDialog
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    uiState: UiState,
    onAddEvent: (
        title: String,
        category: String,
        assignedMemberId: Long?,
        assignedMemberName: String,
        assignedMemberColorHex: String,
        dueDate: Long,
        dayOfWeek: Int,
        isBill: Boolean,
        billAmount: Double?,
        recurrence: String,
        notes: String
    ) -> Unit,
    onToggleCompletion: (RoomEvent) -> Unit,
    onUpdateStatus: (RoomEvent, String) -> Unit,
    onDeleteEvent: (RoomEvent) -> Unit,
    onPayUtilityBill: (RoomEvent, Long, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedViewTab by remember { mutableIntStateOf(0) } // 0 = Checkbox Matrix, 1 = Calendar, 2 = Kanban Board
    var categoryFilter by remember { mutableStateOf("ALL") } // ALL, CLEANING, COOKING, UTILITY_BILL, MINE
    var showAddEventSheet by remember { mutableStateOf(false) }
    var billToPay by remember { mutableStateOf<RoomEvent?>(null) }

    val addEventSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val payBillSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Calendar state
    var calendarMonthOffset by remember { mutableIntStateOf(0) }
    var selectedCalendarDay by remember {
        mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    }

    val displayedEvents = remember(uiState.events, categoryFilter, uiState.currentMember) {
        uiState.events.filter { event ->
            when (categoryFilter) {
                "CLEANING" -> event.category == "CLEANING"
                "COOKING" -> event.category == "COOKING"
                "UTILITY_BILL" -> event.isBill || event.category == "UTILITY_BILL"
                "MINE" -> event.assignedMemberId == uiState.currentMember?.id
                else -> true
            }
        }
    }

    val totalChores = uiState.events.count { !it.isBill }
    val completedChores = uiState.events.count { !it.isBill && it.isCompleted }
    val choreCompletionRatio = if (totalChores > 0) completedChores.toFloat() / totalChores else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("events_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. EVENT SIDE HEADER & ADD ACTION
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("events_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "The Event Side",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Room Life",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Chores • Cooking rotations • Utility bills",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddEventSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_open_add_event")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Duty")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekly Duty Progress & Streak
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "5-Day Room Streak",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF97316)
                            )
                        }
                        Text(
                            text = "$completedChores of $totalChores completed (${(choreCompletionRatio * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { choreCompletionRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // ==========================================
        // 2. UTILITY BILL COUNTDOWNS SHELF
        // ==========================================
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Utility Bill Countdowns",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${uiState.utilityBills.count { !it.isCompleted }} Upcoming",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utility_bills_row")
                ) {
                    items(uiState.utilityBills) { bill ->
                        BillCountdownCard(
                            bill = bill,
                            onPayClick = { billToPay = bill },
                            onToggleCompletion = { onToggleCompletion(bill) }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. SEGMENTED VIEW SELECTOR (SAME PAGE!)
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedViewTab,
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedViewTab == 0,
                        onClick = { selectedViewTab = 0 },
                        text = { Text("Checkbox Matrix", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.CheckBox, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_matrix_view")
                    )
                    Tab(
                        selected = selectedViewTab == 1,
                        onClick = { selectedViewTab = 1 },
                        text = { Text("Calendar Dates", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_calendar_view")
                    )
                    Tab(
                        selected = selectedViewTab == 2,
                        onClick = { selectedViewTab = 2 },
                        text = { Text("Kanban Board", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.ViewKanban, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_kanban_view")
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "ALL" to "All Duties",
                    "CLEANING" to "🧹 Cleaning Only",
                    "COOKING" to "🍳 Cooking Rotations",
                    "UTILITY_BILL" to "⚡ Bills & Rent",
                    "MINE" to "👤 My Duties"
                ).forEach { (key, label) ->
                    item {
                        FilterChip(
                            selected = categoryFilter == key,
                            onClick = { categoryFilter = key },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. TAB CONTENTS ON THE EXACT SAME PAGE
        // ==========================================
        when (selectedViewTab) {
            0 -> {
                // VIEW 1: CHECKBOX MATRIX VIEW
                item {
                    ChoreMatrixSection(
                        events = displayedEvents,
                        currentMemberName = uiState.currentMember?.name ?: "Roommate",
                        onToggleCompletion = onToggleCompletion,
                        onDeleteEvent = onDeleteEvent,
                        onPayBill = { billToPay = it }
                    )
                }
            }

            1 -> {
                // VIEW 2: INTERACTIVE CALENDAR VIEW
                item {
                    InteractiveCalendarSection(
                        events = displayedEvents,
                        monthOffset = calendarMonthOffset,
                        selectedDay = selectedCalendarDay,
                        onMonthOffsetChange = { calendarMonthOffset += it },
                        onSelectDay = { selectedCalendarDay = it },
                        onToggleCompletion = onToggleCompletion,
                        onPayBill = { billToPay = it }
                    )
                }
            }

            2 -> {
                // VIEW 3: KANBAN BOARD VIEW
                item {
                    KanbanBoardSection(
                        events = displayedEvents,
                        onUpdateStatus = onUpdateStatus,
                        onDeleteEvent = onDeleteEvent,
                        onPayBill = { billToPay = it }
                    )
                }
            }
        }
    }

    // Modal dialogs
    if (showAddEventSheet) {
        AddEventDialog(
            roommates = uiState.roommates,
            currentMember = uiState.currentMember,
            sheetState = addEventSheetState,
            onDismiss = { showAddEventSheet = false },
            onAddEvent = onAddEvent
        )
    }

    billToPay?.let { bill ->
        PayBillDialog(
            event = bill,
            currentMember = uiState.currentMember,
            poolBalance = uiState.remainingPoolBalance,
            sheetState = payBillSheetState,
            onDismiss = { billToPay = null },
            onConfirmPay = { event, memberId, memberName, source ->
                onPayUtilityBill(event, memberId, memberName, source)
                billToPay = null
            }
        )
    }
}

// ==========================================
// BILL COUNTDOWN CARD
// ==========================================
@Composable
fun BillCountdownCard(
    bill: RoomEvent,
    onPayClick: () -> Unit,
    onToggleCompletion: () -> Unit
) {
    val now = System.currentTimeMillis()
    val diffMillis = bill.dueDate - now
    val daysRemaining = (diffMillis / (24 * 60 * 60 * 1000L)).toInt()

    val (urgencyColor, urgencyBg, countdownText) = when {
        bill.isCompleted -> Triple(Color(0xFF059669), Color(0xFFECFDF5), "Paid & Cleared ✅")
        daysRemaining < 0 -> Triple(Color(0xFFDC2626), Color(0xFFFEF2F2), "Overdue ${-daysRemaining}d ⚠️")
        daysRemaining == 0 -> Triple(Color(0xFFEA580C), Color(0xFFFFF7ED), "Due Today! 🚨")
        daysRemaining <= 3 -> Triple(Color(0xFFEAB308), Color(0xFFFEFCE8), "$daysRemaining Days Left")
        else -> Triple(Color(0xFF059669), Color(0xFFECFDF5), "$daysRemaining Days Left")
    }

    Card(
        modifier = Modifier
            .width(220.dp)
            .testTag("bill_card_${bill.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = urgencyBg
                ) {
                    Text(
                        text = countdownText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor
                    )
                }

                if (bill.billAmount != null) {
                    Text(
                        text = "₹${String.format("%.0f", bill.billAmount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = bill.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Assigned: ${bill.assignedMemberName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!bill.isCompleted) {
                Button(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pay from Pool", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onToggleCompletion,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cleared", fontSize = 12.sp, color = Color(0xFF059669))
                }
            }
        }
    }
}

// ==========================================
// VIEW 1: CHECKBOX MATRIX VIEW
// ==========================================
@Composable
fun ChoreMatrixSection(
    events: List<RoomEvent>,
    currentMemberName: String,
    onToggleCompletion: (RoomEvent) -> Unit,
    onDeleteEvent: (RoomEvent) -> Unit,
    onPayBill: (RoomEvent) -> Unit
) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val cal = Calendar.getInstance()
    // Current day of week (1=Mon..7=Sun)
    val currentDayIndex = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Table Matrix Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chore_matrix_table_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weekly Chore Checkbox Matrix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap any day to check off duties & log verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Matrix Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duty & Assignee",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.8f).padding(start = 6.dp)
                    )
                    daysOfWeek.forEachIndexed { idx, day ->
                        val isToday = (idx + 1) == currentDayIndex
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Matrix Rows
                events.forEach { event ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(event.assignedMemberColorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title + Member
                        Row(
                            modifier = Modifier.weight(1.8f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = event.assignedMemberName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = event.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = event.assignedMemberName,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 7 Checkbox Days
                        (1..7).forEach { dayNum ->
                            val isScheduledDay = event.dayOfWeek == dayNum || event.recurrence == "DAILY"
                            val isChecked = isScheduledDay && event.isCompleted

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = isScheduledDay) {
                                        onToggleCompletion(event)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isScheduledDay) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isChecked) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isChecked) Color(0xFF059669) else Color.LightGray,
                                                shape = RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "•",
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detailed Cards List
        Text(
            text = "Active Chore Roster & Rotations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        events.forEach { event ->
            ChoreItemCard(
                event = event,
                onToggleCompletion = { onToggleCompletion(event) },
                onDelete = { onDeleteEvent(event) },
                onPayBill = { onPayBill(event) }
            )
        }
    }
}

// ==========================================
// CHORE ITEM CARD
// ==========================================
@Composable
fun ChoreItemCard(
    event: RoomEvent,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onPayBill: () -> Unit
) {
    val categoryIcon = when (event.category) {
        "CLEANING" -> Icons.Default.CleaningServices
        "COOKING" -> Icons.Default.Restaurant
        "UTILITY_BILL" -> Icons.Default.ElectricBolt
        "MAINTENANCE" -> Icons.Default.Handyman
        else -> Icons.Default.ShoppingCart
    }

    val categoryColor = when (event.category) {
        "CLEANING" -> Color(0xFF0284C7)
        "COOKING" -> Color(0xFFEA580C)
        "UTILITY_BILL" -> Color(0xFF7C3AED)
        "MAINTENANCE" -> Color(0xFFD97706)
        else -> Color(0xFF059669)
    }

    val memberColor = try {
        Color(android.graphics.Color.parseColor(event.assignedMemberColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chore_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = event.isCompleted,
                onCheckedChange = { onToggleCompletion() },
                modifier = Modifier.testTag("chk_chore_${event.id}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Main details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(categoryIcon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.category.replace("_", " "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "• ${event.recurrence}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (event.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )

                if (event.notes.isNotBlank()) {
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(memberColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = event.assignedMemberName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.assignedMemberName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (event.isCompleted && event.completedByMemberName != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Verified by ${event.completedByMemberName})",
                            fontSize = 11.sp,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }

            // Quick actions
            if (event.isBill && !event.isCompleted) {
                IconButton(onClick = onPayBill) {
                    Icon(Icons.Default.Payment, contentDescription = "Pay Bill", tint = MaterialTheme.colorScheme.primary)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

// ==========================================
// VIEW 2: INTERACTIVE CALENDAR SECTION
// ==========================================
@Composable
fun InteractiveCalendarSection(
    events: List<RoomEvent>,
    monthOffset: Int,
    selectedDay: Int,
    onMonthOffsetChange: (Int) -> Unit,
    onSelectDay: (Int) -> Unit,
    onToggleCompletion: (RoomEvent) -> Unit,
    onPayBill: (RoomEvent) -> Unit
) {
    val cal = Calendar.getInstance().apply {
        add(Calendar.MONTH, monthOffset)
    }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

    val selectedDateEvents = remember(events, selectedDay, monthOffset) {
        events.filter { event ->
            val eventCal = Calendar.getInstance().apply { timeInMillis = event.dueDate }
            // For recurring weekly events, match day of week
            val isSameDay = eventCal.get(Calendar.DAY_OF_MONTH) == selectedDay
            val isRecurringWeekly = event.recurrence == "WEEKLY"
            val isRecurringDaily = event.recurrence == "DAILY"
            isSameDay || isRecurringDaily || (isRecurringWeekly && (selectedDay % 7 == event.dayOfWeek % 7))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calendar_month_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month Header Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMonthOffsetChange(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month")
                    }

                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { onMonthOffsetChange(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Day Names
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid (up to 6 weeks)
                var currentDay = 1
                for (week in 0..5) {
                    if (currentDay > maxDays) break
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        for (dayOfWeek in 1..7) {
                            if (week == 0 && dayOfWeek < firstDayOfWeek) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else if (currentDay <= maxDays) {
                                val thisDay = currentDay
                                val isSelected = thisDay == selectedDay
                                val hasEvents = events.any { e ->
                                    val c = Calendar.getInstance().apply { timeInMillis = e.dueDate }
                                    c.get(Calendar.DAY_OF_MONTH) == thisDay || e.recurrence == "DAILY"
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                        )
                                        .clickable { onSelectDay(thisDay) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$thisDay",
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (hasEvents) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                                currentDay++
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Schedule on Selected Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Duties Scheduled for Day $selectedDay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "${selectedDateEvents.size} Duties",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (selectedDateEvents.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No duties mapped for this date",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tap '+ Add Duty' above to assign a rotation or bill",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            selectedDateEvents.forEach { event ->
                ChoreItemCard(
                    event = event,
                    onToggleCompletion = { onToggleCompletion(event) },
                    onDelete = {},
                    onPayBill = { onPayBill(event) }
                )
            }
        }
    }
}

// ==========================================
// VIEW 3: KANBAN BOARD SECTION
// ==========================================
@Composable
fun KanbanBoardSection(
    events: List<RoomEvent>,
    onUpdateStatus: (RoomEvent, String) -> Unit,
    onDeleteEvent: (RoomEvent) -> Unit,
    onPayBill: (RoomEvent) -> Unit
) {
    var activeKanbanColumn by remember { mutableStateOf("ALL") } // ALL, TODO, IN_PROGRESS, COMPLETED

    val todoEvents = remember(events) { events.filter { it.status == "TODO" && !it.isCompleted } }
    val inProgressEvents = remember(events) { events.filter { it.status == "IN_PROGRESS" && !it.isCompleted } }
    val completedEvents = remember(events) { events.filter { it.status == "COMPLETED" || it.isCompleted } }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Kanban Column Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "All Columns",
                "TODO" to "To Do (${todoEvents.size})",
                "IN_PROGRESS" to "In Duty (${inProgressEvents.size})",
                "COMPLETED" to "Done (${completedEvents.size})"
            ).forEach { (colKey, colLabel) ->
                FilterChip(
                    selected = activeKanbanColumn == colKey,
                    onClick = { activeKanbanColumn = colKey },
                    label = { Text(colLabel) }
                )
            }
        }

        if (activeKanbanColumn == "ALL" || activeKanbanColumn == "TODO") {
            KanbanColumnGroup(
                title = "To Do / Scheduled",
                badgeColor = Color(0xFF64748B),
                events = todoEvents,
                nextStatus = "IN_PROGRESS",
                nextStatusLabel = "Start Duty",
                onMoveStatus = { e, s -> onUpdateStatus(e, s) },
                onDelete = onDeleteEvent,
                onPayBill = onPayBill
            )
        }

        if (activeKanbanColumn == "ALL" || activeKanbanColumn == "IN_PROGRESS") {
            KanbanColumnGroup(
                title = "In Progress / Active Today",
                badgeColor = Color(0xFF0284C7),
                events = inProgressEvents,
                nextStatus = "COMPLETED",
                nextStatusLabel = "Mark Completed",
                onMoveStatus = { e, s -> onUpdateStatus(e, s) },
                onDelete = onDeleteEvent,
                onPayBill = onPayBill
            )
        }

        if (activeKanbanColumn == "ALL" || activeKanbanColumn == "COMPLETED") {
            KanbanColumnGroup(
                title = "Completed & Verified",
                badgeColor = Color(0xFF059669),
                events = completedEvents,
                nextStatus = "TODO",
                nextStatusLabel = "Reopen",
                onMoveStatus = { e, s -> onUpdateStatus(e, s) },
                onDelete = onDeleteEvent,
                onPayBill = onPayBill
            )
        }
    }
}

@Composable
fun KanbanColumnGroup(
    title: String,
    badgeColor: Color,
    events: List<RoomEvent>,
    nextStatus: String,
    nextStatusLabel: String,
    onMoveStatus: (RoomEvent, String) -> Unit,
    onDelete: (RoomEvent) -> Unit,
    onPayBill: (RoomEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${events.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (events.isEmpty()) {
                Text(
                    text = "No duties in this column",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                events.forEach { event ->
                    KanbanCardItem(
                        event = event,
                        nextStatus = nextStatus,
                        nextStatusLabel = nextStatusLabel,
                        onMove = { onMoveStatus(event, nextStatus) },
                        onDelete = { onDelete(event) },
                        onPay = { onPayBill(event) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun KanbanCardItem(
    event: RoomEvent,
    nextStatus: String,
    nextStatusLabel: String,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onPay: () -> Unit
) {
    val memberColor = try {
        Color(android.graphics.Color.parseColor(event.assignedMemberColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = event.category.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (event.billAmount != null) {
                    Text(
                        text = "₹${String.format("%.2f", event.billAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (event.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(memberColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = event.assignedMemberName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.assignedMemberName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (event.isBill && !event.isCompleted) {
                        FilledTonalButton(
                            onClick = onPay,
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Pay", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onMove,
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(nextStatusLabel, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
