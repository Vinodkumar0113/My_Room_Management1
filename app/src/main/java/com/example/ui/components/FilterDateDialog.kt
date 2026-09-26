package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.FilterTimePeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDateDialog(
    currentPeriod: FilterTimePeriod,
    customStart: Long,
    customEnd: Long,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelectPeriod: (FilterTimePeriod) -> Unit,
    onSelectCustomRange: (Long, Long) -> Unit
) {
    var selectedPeriod by remember { mutableStateOf(currentPeriod) }
    var showDatePickerModal by remember { mutableStateOf(false) }

    val datePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = customStart,
        initialSelectedEndDateMillis = customEnd
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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Statistics & Expenses by Date",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_date_filter")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PeriodOptionRow(
                label = "Current Month (This Month)",
                selected = selectedPeriod == FilterTimePeriod.THIS_MONTH,
                onClick = { selectedPeriod = FilterTimePeriod.THIS_MONTH }
            )

            PeriodOptionRow(
                label = "Previous Month (Last Month)",
                selected = selectedPeriod == FilterTimePeriod.LAST_MONTH,
                onClick = { selectedPeriod = FilterTimePeriod.LAST_MONTH }
            )

            PeriodOptionRow(
                label = "All Time",
                selected = selectedPeriod == FilterTimePeriod.ALL_TIME,
                onClick = { selectedPeriod = FilterTimePeriod.ALL_TIME }
            )

            PeriodOptionRow(
                label = "Custom Date Range",
                selected = selectedPeriod == FilterTimePeriod.CUSTOM,
                onClick = {
                    selectedPeriod = FilterTimePeriod.CUSTOM
                    showDatePickerModal = true
                }
            )

            if (selectedPeriod == FilterTimePeriod.CUSTOM) {
                val startStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(customStart))
                val endStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(customEnd))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Custom Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " Range: $startStr to $endStr",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { showDatePickerModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Text("Select Custom Start & End Dates")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (selectedPeriod == FilterTimePeriod.CUSTOM) {
                        onSelectCustomRange(
                            datePickerState.selectedStartDateMillis ?: customStart,
                            datePickerState.selectedEndDateMillis ?: customEnd
                        )
                    } else {
                        onSelectPeriod(selectedPeriod)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_date_filter"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Date Filter", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePickerModal) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            confirmButton = {
                Button(
                    onClick = {
                        val start = datePickerState.selectedStartDateMillis ?: customStart
                        val end = datePickerState.selectedEndDateMillis ?: customEnd
                        onSelectCustomRange(start, end)
                        showDatePickerModal = false
                    }
                ) {
                    Text("Save Dates")
                }
            },
            dismissButton = {
                IconButton(onClick = { showDatePickerModal = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PeriodOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
