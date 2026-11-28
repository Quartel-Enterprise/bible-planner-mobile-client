package com.quare.bibleplanner.android.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.core.date.DateRepresentation
import com.quare.bibleplanner.core.date.RelativeTime
import com.quare.bibleplanner.feature.chat.presentation.model.ChatConversationBucket
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun relativeTimeLabel(relativeTime: RelativeTime): String = when (relativeTime) {
    RelativeTime.JustNow -> stringResource(R.string.chat_relative_active_now)

    is RelativeTime.MinutesAgo -> pluralStringResource(
        R.plurals.chat_relative_minutes_ago,
        relativeTime.minutes,
        relativeTime.minutes,
    )

    is RelativeTime.HoursAgo -> pluralStringResource(
        R.plurals.chat_relative_hours_ago,
        relativeTime.hours,
        relativeTime.hours,
    )

    is RelativeTime.OlderThanADay -> dateRepresentationLabel(relativeTime.representation)
}

@Composable
internal fun bucketLabel(bucket: ChatConversationBucket): String = when (bucket) {
    ChatConversationBucket.Today -> stringResource(R.string.chat_bucket_today)
    ChatConversationBucket.Yesterday -> stringResource(R.string.chat_bucket_yesterday)
    ChatConversationBucket.LastSevenDays -> stringResource(R.string.chat_bucket_last_seven_days)
    is ChatConversationBucket.InMonth -> stringResource(bucket.month.toStringRes())
}

@Composable
private fun dateRepresentationLabel(representation: DateRepresentation): String = when (representation) {
    DateRepresentation.Today -> stringResource(R.string.chat_date_today)

    DateRepresentation.Yesterday -> stringResource(R.string.chat_date_yesterday)

    DateRepresentation.Tomorrow -> stringResource(R.string.chat_date_tomorrow)

    is DateRepresentation.DaysAgo -> pluralStringResource(
        R.plurals.chat_date_days_ago,
        representation.days,
        representation.days,
    )

    DateRepresentation.LastWeek -> stringResource(R.string.chat_date_last_week)

    is DateRepresentation.WeeksAgo -> pluralStringResource(
        R.plurals.chat_date_weeks_ago,
        representation.weeks,
        representation.weeks,
    )

    DateRepresentation.LastMonth -> stringResource(R.string.chat_date_last_month)

    is DateRepresentation.MonthsAgo -> pluralStringResource(
        R.plurals.chat_date_months_ago,
        representation.months,
        representation.months,
    )

    is DateRepresentation.Custom -> rememberMediumDateLabel(representation.date)
}

// Why: the app language can differ from the system's, and the configuration carries the one the app shows.
@Composable
private fun rememberMediumDateLabel(date: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(date, locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date.toJavaLocalDate())
    }
}

private fun Month.toStringRes(): Int = when (this) {
    Month.JANUARY -> R.string.chat_january
    Month.FEBRUARY -> R.string.chat_february
    Month.MARCH -> R.string.chat_march
    Month.APRIL -> R.string.chat_april
    Month.MAY -> R.string.chat_may
    Month.JUNE -> R.string.chat_june
    Month.JULY -> R.string.chat_july
    Month.AUGUST -> R.string.chat_august
    Month.SEPTEMBER -> R.string.chat_september
    Month.OCTOBER -> R.string.chat_october
    Month.NOVEMBER -> R.string.chat_november
    Month.DECEMBER -> R.string.chat_december
}
