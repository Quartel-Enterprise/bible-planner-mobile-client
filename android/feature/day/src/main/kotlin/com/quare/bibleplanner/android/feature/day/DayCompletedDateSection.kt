package com.quare.bibleplanner.android.feature.day

import android.text.format.DateFormat
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.model.DatePresentationModel
import kotlinx.datetime.number
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val TIME_SKELETON_24_HOURS = "Hm"
private const val TIME_SKELETON_12_HOURS = "hma"

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
    val locale = LocalConfiguration.current.locales[0]
    // Why: the locale alone picks 12 or 24 hours, while the user's clock setting must win.
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    return remember(date, locale, is24Hour) {
        val dateTime = LocalDateTime.of(
            date.year,
            date.month.number,
            date.day,
            date.hour.toIntOrNull() ?: 0,
            date.minute.toIntOrNull() ?: 0,
        )
        val datePart = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(dateTime)
        val timeSkeleton = if (is24Hour) TIME_SKELETON_24_HOURS else TIME_SKELETON_12_HOURS
        val timePart = DateTimeFormatter
            .ofPattern(DateFormat.getBestDateTimePattern(locale, timeSkeleton), locale)
            .format(dateTime)
        "$datePart, $timePart"
    }
}
