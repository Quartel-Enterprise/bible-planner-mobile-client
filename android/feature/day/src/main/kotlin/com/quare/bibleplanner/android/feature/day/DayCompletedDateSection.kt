package com.quare.bibleplanner.android.feature.day

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.date.rememberDateTimeLabel
import com.quare.bibleplanner.feature.day.presentation.model.DatePresentationModel
import kotlinx.datetime.LocalDateTime

@Composable
internal fun DayCompletedDateSection(
    isRead: Boolean,
    formattedReadDate: DatePresentationModel?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isRead,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.day_completed_date),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.day_calendar),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedReadDate?.let { rememberFormattedReadDate(it) }
                            ?: stringResource(R.string.day_no_date_set),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = onEditClick) {
                        Text(text = stringResource(R.string.day_edit))
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberFormattedReadDate(date: DatePresentationModel): String {
    val dateTime = remember(date) {
        LocalDateTime(
            year = date.year,
            month = date.month,
            day = date.day,
            hour = date.hour.toIntOrNull() ?: 0,
            minute = date.minute.toIntOrNull() ?: 0,
        )
    }
    return rememberDateTimeLabel(dateTime)
}
