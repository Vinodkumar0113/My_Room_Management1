package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Roommate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    roommates: List<Roommate>,
    currentMember: Roommate?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("CLEANING") } // CLEANING, COOKING, UTILITY_BILL, MAINTENANCE, GROCERY_RUN
    var selectedMember by remember { mutableStateOf(currentMember ?: roommates.firstOrNull()) }
    var recurrence by remember { mutableStateOf("WEEKLY") } // ONCE, DAILY, WEEKLY, MONTHLY
    var isBill by remember { mutableStateOf(false) }
    var billAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var daysFromNow by remember { mutableIntStateOf(1) } // 0=today, 1=tomorrow, 3, 7
    var selectedDayOfWeek by remember { mutableIntStateOf(1) } // 1=Mon .. 7=Sun

    val categories = listOf(
        Triple("CLEANING", "Cleaning Duty", Icons.Default.CleaningServices),
        Triple("COOKING", "Cooking Chef", Icons.Default.Restaurant),
        Triple("UTILITY_BILL", "Utility Bill", Icons.Default.ElectricBolt),
        Triple("MAINTENANCE", "Maintenance", Icons.Default.Handyman),
        Triple("GROCERY_RUN", "Grocery Run", Icons.Default.ShoppingCart)
    )

    val recurrences = listOf("DAILY", "WEEKLY", "MONTHLY", "ONCE")
    val daysLabels = listOf(
        1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_event_sheet")
        ) {
            Text(
                text = "Add Room Duty or Event",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Map cleaning schedules, cooking rotations, or bill countdowns",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (catKey, catLabel, icon) ->
                    FilterChip(
                        selected = selectedCategory == catKey,
                        onClick = {
                            selectedCategory = catKey
                            if (catKey == "UTILITY_BILL") {
                                isBill = true
                            }
                        },
                        label = { Text(catLabel) },
                        leadingIcon = {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        when (selectedCategory) {
                            "CLEANING" -> "Duty Title (e.g. Kitchen deep clean, Living room sweep)"
                            "COOKING" -> "Cooking Event (e.g. Dinner chef: Mexican bowls)"
                            "UTILITY_BILL" -> "Bill Name (e.g. Electricity bill, WiFi recharge)"
                            else -> "Event / Task Title"
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_event_title"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Assign To Roommate
            Text(
                text = "Assign Roommate",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(roommates) { member ->
                    val isSelected = selectedMember?.id == member.id
                    val color = try {
                        Color(android.graphics.Color.parseColor(member.avatarColorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMember = member },
                        label = { Text(member.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Due Date Offset
            Text(
                text = "Due Countdown / Schedule",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    0 to "Today",
                    1 to "Tomorrow",
                    3 to "In 3 Days",
                    7 to "In 7 Days"
                ).forEach { (offset, label) ->
                    FilterChip(
                        selected = daysFromNow == offset,
                        onClick = { daysFromNow = offset },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of week
            Text(
                text = "Day of Week (for Matrix)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysLabels) { (dayNum, dayName) ->
                    FilterChip(
                        selected = selectedDayOfWeek == dayNum,
                        onClick = { selectedDayOfWeek = dayNum },
                        label = { Text(dayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recurrence
            Text(
                text = "Recurrence",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                recurrences.forEach { rec ->
                    FilterChip(
                        selected = recurrence == rec,
                        onClick = { recurrence = rec },
                        label = { Text(rec.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Is Utility Bill toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Is this a payable utility bill?",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Enables live countdown and 1-tap payment from shared pool",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isBill,
                    onCheckedChange = { isBill = it }
                )
            }

            if (isBill) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = billAmountText,
                    onValueChange = { billAmountText = it },
                    label = { Text("Bill Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Instructions (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val now = System.currentTimeMillis()
                            val dueTimestamp = now + (daysFromNow * 24 * 60 * 60 * 1000L)
                            val billAmount = billAmountText.toDoubleOrNull()
                            onAddEvent(
                                title.trim(),
                                selectedCategory,
                                selectedMember?.id,
                                selectedMember?.name ?: "Unassigned",
                                selectedMember?.avatarColorHex ?: "#4F46E5",
                                dueTimestamp,
                                selectedDayOfWeek,
                                isBill,
                                billAmount,
                                recurrence,
                                notes.trim()
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("btn_confirm_add_event"),
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Duty / Event")
                }
            }
        }
    }
}
