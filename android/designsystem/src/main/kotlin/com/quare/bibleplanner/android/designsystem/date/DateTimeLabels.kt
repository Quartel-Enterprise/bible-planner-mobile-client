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
import kotlinx.datetime.Month
import kotlinx.datetime.number
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// Why: the skeletons android.text.format.DateFormat.getTimeFormat picks, so times read as in the system apps.
private const val TIME_SKELETON_24_HOURS = "Hm"
private const val TIME_SKELETON_12_HOURS = "hm"
private const val MONTH_SKELETON = "LLLL"
private const val MONTH_YEAR_SKELETON = "MMMMy"
private const val SHORT_DATE_SKELETON = "dMMM"
private const val SHORT_DATE_YEAR_SKELETON = "dMMMy"
private const val ANY_YEAR = 2000
private const val FIRST_DAY = 1

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

// Why: a month heads a group of dates, so it starts with a capital even where the locale writes it in lowercase.
@Composable
fun rememberMonthLabel(
    month: Month,
    year: Int?,
): String {
    val locale = currentAppLocale()
    return remember(month, year, locale) {
        formatDateWithIcu(
            // Why: a month with no year still needs a full date to format, and the first day exists in every month.
            date = LocalDate(
                year = year ?: ANY_YEAR,
                month = month,
                day = FIRST_DAY,
            ),
            skeleton = if (year == null) MONTH_SKELETON else MONTH_YEAR_SKELETON,
            locale = locale,
        ).replaceFirstChar { it.titlecase(locale) }
    }
}

@Composable
fun rememberShortDateLabel(
    date: LocalDate,
    isYearShown: Boolean,
): String {
    val locale = currentAppLocale()
    return remember(date, isYearShown, locale) {
        formatDateWithIcu(
            date = date,
            skeleton = if (isYearShown) SHORT_DATE_YEAR_SKELETON else SHORT_DATE_SKELETON,
            locale = locale,
        )
    }
}

private fun formatDateWithIcu(
    date: LocalDate,
    skeleton: String,
    locale: Locale,
): String {
    val calendar = Calendar.getInstance(locale).apply {
        clear()
        set(date.year, date.month.number - 1, date.day)
    }
    return SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(calendar.time)
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
