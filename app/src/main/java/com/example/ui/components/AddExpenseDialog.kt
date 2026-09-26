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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.EmeraldPositive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    roommates: List<Roommate>,
    currentMember: Roommate? = null,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddExpense: (
        title: String,
        amount: Double,
        paidByMemberId: Long,
        paidByMemberName: String,
        category: String,
        isRoomExpense: Boolean,
        paymentSource: String,
        notes: String,
        paymentMethod: String,
        recipientDetail: String
    ) -> Unit
) {
    var paymentMode by remember { mutableStateOf("DIRECT") } // "DIRECT" or "PHONE_PAY"
    var phoneNumber by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var selectedMember by remember { mutableStateOf(currentMember ?: roommates.firstOrNull()) }
    var isRoomExpense by remember { mutableStateOf(true) }
    var paymentSource by remember { mutableStateOf("POOL") } // "POOL" or "OUT_OF_POCKET"
    var notes by remember { mutableStateOf("") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Groceries", "Room Rent", "Electricity", "Water", "Wifi", "Maintenance", "Food", "Personal", "Other"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (paymentMode == "PHONE_PAY") "Pay to Phone Number" else "Record Spend / Pay",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_expense")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spend Mode Selector (Direct Record vs Pay to Phone Number)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = paymentMode == "DIRECT",
                    onClick = { paymentMode = "DIRECT" },
                    label = { Text("📝 Standard Spend") },
                    modifier = Modifier.testTag("mode_direct_spend")
                )
                FilterChip(
                    selected = paymentMode == "PHONE_PAY",
                    onClick = {
                        paymentMode = "PHONE_PAY"
                        if (title.isBlank()) title = "Phone Payment"
                    },
                    label = { Text("📱 Pay to Phone Number") },
                    modifier = Modifier.testTag("mode_phone_pay")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number Input section if in PHONE_PAY mode
            if (paymentMode == "PHONE_PAY") {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Recipient Phone Number / UPI Mobile") },
                    placeholder = { Text("+91 98765 43210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pay_phone_number_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Phone selector from Roommates
                Text(
                    text = "Or pick roommate contact:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(roommates.filter { it.phoneOrNote.isNotBlank() }) { mem ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                phoneNumber = mem.phoneOrNote
                                title = "Transfer to ${mem.name}"
                            }
                        ) {
                            Text(
                                text = "${mem.name} (${mem.phoneOrNote})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (paymentMode == "PHONE_PAY") "Purpose / Payment For (e.g. Dinner share, Milk)" else "Expense Title (e.g. Vegetables, Rent, Dinner)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_title_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_amount_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Expense Scope: Room Shared vs Personal
            Text(
                text = "Expense Type",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isRoomExpense,
                    onClick = {
                        isRoomExpense = true
                        if (selectedCategory == "Personal") selectedCategory = "Groceries"
                    },
                    label = { Text("🏠 Room Shared") }
                )
                FilterChip(
                    selected = !isRoomExpense,
                    onClick = {
                        isRoomExpense = false
                        selectedCategory = "Personal"
                    },
                    label = { Text("👤 Personal Spend") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Selector
            if (isRoomExpense) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { categoryDropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Spent By Member
            Text(
                text = "Spent / Authorized By",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { memberDropdownExpanded = true }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedMember?.name ?: "Select Roommate",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Member")
                }

                DropdownMenu(
                    expanded = memberDropdownExpanded,
                    onDismissRequest = { memberDropdownExpanded = false }
                ) {
                    roommates.forEach { member ->
                        DropdownMenuItem(
                            text = { Text(member.name + if (member.isAdmin) " 👑 (Admin)" else "") },
                            onClick = {
                                selectedMember = member
                                memberDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Source: Debit from Shared Room Wallet vs Out-of-Pocket
            Text(
                text = "Payment Source",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { paymentSource = "POOL" }
                ) {
                    RadioButton(
                        selected = paymentSource == "POOL",
                        onClick = { paymentSource = "POOL" }
                    )
                    Text("💳 Debit from Shared Room Wallet")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { paymentSource = "OUT_OF_POCKET" }
                ) {
                    RadioButton(
                        selected = paymentSource == "OUT_OF_POCKET",
                        onClick = { paymentSource = "OUT_OF_POCKET" }
                    )
                    Text("💵 Paid Individually (Out-of-Pocket)")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(18.dp))

            val amount = amountText.toDoubleOrNull() ?: 0.0
            val isPhoneValid = paymentMode != "PHONE_PAY" || phoneNumber.isNotBlank()
            val isValid = title.isNotBlank() && amount > 0 && selectedMember != null && isPhoneValid

            Button(
                onClick = {
                    val member = selectedMember ?: return@Button
                    val finalCategory = if (!isRoomExpense) "Personal" else selectedCategory
                    val method = if (paymentMode == "PHONE_PAY") "PHONE_NUMBER" else "DIRECT"
                    val recipient = if (paymentMode == "PHONE_PAY") "Phone: $phoneNumber" else ""
                    onAddExpense(
                        title,
                        amount,
                        member.id,
                        member.name,
                        finalCategory,
                        isRoomExpense,
                        paymentSource,
                        notes,
                        method,
                        recipient
                    )
                    onDismiss()
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_add_expense"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (paymentMode == "PHONE_PAY") "Pay & Debit from Wallet" else "Confirm & Record Spend",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
