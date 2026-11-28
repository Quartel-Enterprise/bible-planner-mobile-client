package com.quare.bibleplanner.android.designsystem.date

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// Why: the skeletons android.text.format.DateFormat.getTimeFormat picks, so times read as in the system apps.
private const val TIME_SKELETON_24_HOURS = "Hm"
private const val TIME_SKELETON_12_HOURS = "hm"

// Why: the app language can differ from the system's, and the configuration carries the one the app shows.
@Composable
fun currentAppLocale(): Locale = LocalConfiguration.current.locales[0]

// Why: the locale alone picks 12 or 24 hours, while the user's clock setting must win.
@Composable
fun isSystem24HourFormat(): Boolean = DateFormat.is24HourFormat(LocalContext.current)

@Composable
fun rememberMediumDateLabel(date: LocalDate): String {
    val locale = currentAppLocale()
    return remember(date, locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date.toJavaLocalDate())
    }
}

@Composable
fun rememberTimeLabel(time: LocalTime): String {
    val locale = currentAppLocale()
    val is24Hour = isSystem24HourFormat()
    return remember(time, locale, is24Hour) {
        val skeleton = if (is24Hour) TIME_SKELETON_24_HOURS else TIME_SKELETON_12_HOURS
        DateTimeFormatter
            .ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
            .format(time.toJavaLocalTime())
    }
}

@Composable
fun rememberDateTimeLabel(dateTime: LocalDateTime): String = joinDateAndTime(
    date = rememberMediumDateLabel(dateTime.date),
    time = rememberTimeLabel(dateTime.time),
)

@Composable
fun relativeDateTimeLabel(dateTime: LocalDateTime): String = joinDateAndTime(
    date = relativeDateLabel(dateTime.date),
    time = rememberTimeLabel(dateTime.time),
)

private fun joinDateAndTime(
    date: String,
    time: String,
): String = "$date, $time"
