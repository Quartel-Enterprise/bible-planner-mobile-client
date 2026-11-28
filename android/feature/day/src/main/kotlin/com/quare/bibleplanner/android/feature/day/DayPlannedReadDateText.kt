package com.quare.bibleplanner.android.feature.day

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun DayPlannedReadDateText(
    plannedReadDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    // Why: the app language can differ from the system's, and the configuration carries the one the app shows.
    val locale = LocalConfiguration.current.locales[0]
    val formattedDate = remember(plannedReadDate, locale) {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .format(plannedReadDate.toJavaLocalDate())
    }
    Text(
        text = "${stringResource(R.string.day_planned_read_date)} $formattedDate",
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    )
}
