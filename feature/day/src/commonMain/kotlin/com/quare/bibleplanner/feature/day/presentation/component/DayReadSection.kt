package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.completed_date
import bibleplanner.feature.day.generated.resources.edit
import bibleplanner.feature.day.generated.resources.mark_day_as_read
import bibleplanner.feature.day.generated.resources.no_date_set
import com.quare.bibleplanner.ui.utils.DateTimePickerDialog
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalTime::class)
@Composable
internal fun DayReadSection(
    isRead: Boolean,
    readTimestamp: Long?,
    onToggle: (Boolean) -> Unit,
    onEditDate: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DateTimePickerDialog(
            initialTimestamp = readTimestamp,
            onDismiss = { showDatePicker = false },
            onConfirm = { timestamp ->
                onEditDate(timestamp)
                showDatePicker = false
            },
        )
    }

    Column(
        modifier = modifier.padding(vertical = 16.dp),
    ) {
        // Mark day as read toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.mark_day_as_read),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = isRead,
                onCheckedChange = onToggle,
            )
        }

        // Completed date section - show when day is marked as read
        if (isRead) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.completed_date),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = readTimestamp?.let { formatReadDate(it) } ?: stringResource(Res.string.no_date_set),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            showDatePicker = true
                        },
                    ) {
                        Text(stringResource(Res.string.edit))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun formatReadDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    // Format: "26 Nov 2025, 22:47:30"
    val day = localDateTime.day
    val month = localDateTime.month.name.take(3) // First 3 letters of month
    val year = localDateTime.year
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')
    return "$day $month $year, $hour:$minute:$second"
}
