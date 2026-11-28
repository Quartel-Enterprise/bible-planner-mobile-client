package com.quare.bibleplanner.android.designsystem.date

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
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
        formatTimeWithIcu(
            time = time,
            pattern = DateFormat.getBestDateTimePattern(locale, skeleton),
            locale = locale,
        )
    }
}

/*
 * Why: the best pattern comes from ICU and can hold letters such as B (flexible day period) that java.time
 * doesn't parse before API 34, so ICU formats it too.
 */
private fun formatTimeWithIcu(
    time: LocalTime,
    pattern: String,
    locale: Locale,
): String {
    val calendar = Calendar.getInstance(locale).apply {
        clear()
        set(Calendar.HOUR_OF_DAY, time.hour)
        set(Calendar.MINUTE, time.minute)
    }
    return SimpleDateFormat(pattern, locale).format(calendar.time)
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
