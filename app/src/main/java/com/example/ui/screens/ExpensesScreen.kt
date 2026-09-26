package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Expense
import com.example.ui.components.ExpenseItemCard
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    uiState: UiState,
    onSearchQueryChange: (String) -> Unit,
    onExpenseTypeFilterChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenDateFilter: () -> Unit,
    onSettleExpense: (Expense) -> Unit,
    onApproveExpense: (Expense) -> Unit,
    onRejectExpense: (Expense) -> Unit,
    onIgnoreExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("ALL", "Groceries", "Room Rent", "Electricity", "Water", "Wifi", "Maintenance", "Food", "Personal", "Other")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_expense")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Spend")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title Header & Date Range Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transactions & Spends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Shared Room Wallet Ledger • All Records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onOpenDateFilter, modifier = Modifier.testTag("date_filter_icon")) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Filter Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search input field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search rent, groceries, buyer, phone...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_expense_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (ALL, UNSETTLED, ROOM, SETTLED, PERSONAL)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedExpenseType == "ALL",
                        onClick = { onExpenseTypeFilterChange("ALL") },
                        label = { Text("All (${uiState.expenses.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedExpenseType == "UNSETTLED",
                        onClick = { onExpenseTypeFilterChange("UNSETTLED") },
                        label = { Text("⚠️ Pending Review (${uiState.unsettledPersonalSpends.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedExpenseType == "ROOM",
                        onClick = { onExpenseTypeFilterChange("ROOM") },
                        label = { Text("🏠 Room Shared") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedExpenseType == "SETTLED",
                        onClick = { onExpenseTypeFilterChange("SETTLED") },
                        label = { Text("✅ Settled") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedExpenseType == "PERSONAL",
                        onClick = { onExpenseTypeFilterChange("PERSONAL") },
                        label = { Text("👤 All Personal") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory.equals(cat, ignoreCase = true),
                        onClick = { onCategoryFilterChange(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expense List
            if (uiState.filteredExpenses.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "No Results",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing search filters or changing the selected category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredExpenses) { expense ->
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
            }
        }
    }
}
