package com.quare.bibleplanner.android.feature.accountdetails

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.core.date.DateRepresentation
import com.quare.bibleplanner.core.date.RelativeTime
import com.quare.bibleplanner.core.date.toDateRepresentation
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Instant

private const val TIME_SKELETON_24_HOURS = "Hm"
private const val TIME_SKELETON_12_HOURS = "hm"

@Composable
internal fun rememberLastLoginLabel(instant: Instant): String {
    val dateTime = remember(instant) { instant.toLocalDateTime(TimeZone.currentSystemDefault()) }
    // Why: the representation is relative to today, so caching it per instant would keep "Today" past midnight.
    val dateLabel = dateRepresentationLabel(dateTime.date.toDateRepresentation())
    val locale = currentLocale()
    // Why: the locale alone picks 12 or 24 hours, while the user's clock setting must win.
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val timeLabel = remember(dateTime, locale, is24Hour) {
        val skeleton = if (is24Hour) TIME_SKELETON_24_HOURS else TIME_SKELETON_12_HOURS
        DateTimeFormatter
            .ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
            .format(dateTime.time.toJavaLocalTime())
    }
    return "$dateLabel, $timeLabel"
}

@Composable
internal fun rememberCreatedAtLabel(instant: Instant): String {
    val date = remember(instant) { instant.toLocalDateTime(TimeZone.currentSystemDefault()).date }
    return rememberMediumDateLabel(date)
}

@Composable
internal fun relativeTimeLabel(relativeTime: RelativeTime): String = when (relativeTime) {
    RelativeTime.JustNow -> stringResource(R.string.account_details_active_now)

    is RelativeTime.MinutesAgo -> pluralStringResource(
        R.plurals.account_details_minutes_ago,
        relativeTime.minutes,
        relativeTime.minutes,
    )

    is RelativeTime.HoursAgo -> pluralStringResource(
        R.plurals.account_details_hours_ago,
        relativeTime.hours,
        relativeTime.hours,
    )

    is RelativeTime.OlderThanADay -> dateRepresentationLabel(relativeTime.representation)
}

@Composable
private fun dateRepresentationLabel(representation: DateRepresentation): String = when (representation) {
    DateRepresentation.Today -> stringResource(R.string.account_details_today)

    DateRepresentation.Yesterday -> stringResource(R.string.account_details_yesterday)

    DateRepresentation.Tomorrow -> stringResource(R.string.account_details_tomorrow)

    is DateRepresentation.DaysAgo -> pluralStringResource(
        R.plurals.account_details_days_ago,
        representation.days,
        representation.days,
    )

    DateRepresentation.LastWeek -> stringResource(R.string.account_details_last_week)

    is DateRepresentation.WeeksAgo -> pluralStringResource(
        R.plurals.account_details_weeks_ago,
        representation.weeks,
        representation.weeks,
    )

    DateRepresentation.LastMonth -> stringResource(R.string.account_details_last_month)

    is DateRepresentation.MonthsAgo -> pluralStringResource(
        R.plurals.account_details_months_ago,
        representation.months,
        representation.months,
    )

    is DateRepresentation.Custom -> rememberMediumDateLabel(representation.date)
}

@Composable
private fun rememberMediumDateLabel(date: LocalDate): String {
    val locale = currentLocale()
    return remember(date, locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date.toJavaLocalDate())
    }
}

// Why: the app language can differ from the system's, and the configuration carries the one the app shows.
@Composable
private fun currentLocale(): Locale = LocalConfiguration.current.locales[0]
