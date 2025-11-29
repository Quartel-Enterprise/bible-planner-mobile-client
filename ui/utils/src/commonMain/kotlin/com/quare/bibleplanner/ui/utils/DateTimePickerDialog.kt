package com.quare.bibleplanner.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A date and time picker dialog for Compose Multiplatform.
 * Allows users to select a date and time, returning the selected timestamp in epoch milliseconds.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DateTimePickerDialog(
    initialTimestamp: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val initialDateTime = if (initialTimestamp != null) {
        Instant.fromEpochMilliseconds(initialTimestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    } else {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    var selectedDate by remember {
        mutableIntStateOf(initialDateTime.date.dayOfYear)
    }
    var selectedYear by remember {
        mutableIntStateOf(initialDateTime.year)
    }
    var selectedMonth by remember {
        mutableIntStateOf(initialDateTime.monthNumber)
    }
    var selectedDay by remember {
        mutableIntStateOf(initialDateTime.dayOfMonth)
    }
    var selectedHour by remember {
        mutableIntStateOf(initialDateTime.hour)
    }
    var selectedMinute by remember {
        mutableIntStateOf(initialDateTime.minute)
    }
    var selectedSecond by remember {
        mutableIntStateOf(initialDateTime.second)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Date and Time")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Date selection
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Year
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Year",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        NumberPicker(
                            value = selectedYear,
                            onValueChange = { selectedYear = it },
                            range = 1900..2100,
                        )
                    }

                    // Month
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Month",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        NumberPicker(
                            value = selectedMonth,
                            onValueChange = { selectedMonth = it },
                            range = 1..12,
                        )
                    }

                    // Day
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Day",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val maxDays = getDaysInMonth(selectedYear, selectedMonth)
                        NumberPicker(
                            value = selectedDay,
                            onValueChange = { selectedDay = it.coerceIn(1, maxDays) },
                            range = 1..maxDays,
                        )
                    }
                }

                // Time selection
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Hour
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hour",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        NumberPicker(
                            value = selectedHour,
                            onValueChange = { selectedHour = it },
                            range = 0..23,
                        )
                    }

                    // Minute
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Minute",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        NumberPicker(
                            value = selectedMinute,
                            onValueChange = { selectedMinute = it },
                            range = 0..59,
                        )
                    }

                    // Second
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Second",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        NumberPicker(
                            value = selectedSecond,
                            onValueChange = { selectedSecond = it },
                            range = 0..59,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val localDate = LocalDate(selectedYear, selectedMonth, selectedDay)
                        val localTime = LocalTime(selectedHour, selectedMinute, selectedSecond)
                        val timeZone = TimeZone.currentSystemDefault()
                        // Convert LocalDateTime to Instant by using the date's start of day and adding the time
                        val startOfDay = localDate.atStartOfDayIn(timeZone)
                        val timeOffsetMillis = (
                            localTime.hour * 3600_000L +
                                localTime.minute * 60_000L +
                                localTime.second * 1_000L
                        )
                        val duration: Duration = timeOffsetMillis.milliseconds
                        val instant = startOfDay + duration
                        onConfirm(instant.toEpochMilliseconds())
                    } catch (e: Exception) {
                        // If invalid date/time, use current time
                        onConfirm(kotlin.time.Clock.System.now().toEpochMilliseconds())
                    }
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedButton(
            onClick = {
                val newValue = if (value > range.first) {
                    value - 1
                } else {
                    range.last
                }
                onValueChange(newValue)
            },
            modifier = Modifier.width(60.dp),
        ) {
            Text("−")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                val newValue = if (value < range.last) {
                    value + 1
                } else {
                    range.first
                }
                onValueChange(newValue)
            },
            modifier = Modifier.width(60.dp),
        ) {
            Text("+")
        }
    }
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

