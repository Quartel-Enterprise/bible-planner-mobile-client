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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
internal fun DayReadSection(
    isRead: Boolean,
    readTimestamp: Long?,
    onToggle: (Boolean) -> Unit,
    onEditDate: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                text = "Mark day as read",
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = if (readTimestamp != null) {
                        formatReadDate(readTimestamp)
                    } else {
                        // If no timestamp, use current time for display
                        formatReadDate(Clock.System.now().toEpochMilliseconds())
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        // TODO: Open date picker
                        // For now, update to current time
                        val timestamp = Clock.System.now().toEpochMilliseconds()
                        onEditDate(timestamp)
                    },
                ) {
                    Text("Edit")
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
