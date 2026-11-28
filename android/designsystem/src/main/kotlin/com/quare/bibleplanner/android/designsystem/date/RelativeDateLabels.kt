package com.quare.bibleplanner.android.designsystem.date

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.R
import com.quare.bibleplanner.core.date.DateRepresentation
import com.quare.bibleplanner.core.date.RelativeTime
import com.quare.bibleplanner.core.date.toDateRepresentation
import kotlinx.datetime.LocalDate

@Composable
fun relativeTimeLabel(relativeTime: RelativeTime): String = when (relativeTime) {
    RelativeTime.JustNow -> stringResource(R.string.date_active_now)

    is RelativeTime.MinutesAgo -> pluralStringResource(
        R.plurals.date_minutes_ago,
        relativeTime.minutes,
        relativeTime.minutes,
    )

    is RelativeTime.HoursAgo -> pluralStringResource(
        R.plurals.date_hours_ago,
        relativeTime.hours,
        relativeTime.hours,
    )

    is RelativeTime.OlderThanADay -> dateRepresentationLabel(relativeTime.representation)
}

// Why: the representation is relative to today, so caching it per date would keep "Today" past midnight.
@Composable
fun relativeDateLabel(date: LocalDate): String = dateRepresentationLabel(date.toDateRepresentation())

@Composable
fun dateRepresentationLabel(representation: DateRepresentation): String = when (representation) {
    DateRepresentation.Today -> stringResource(R.string.date_today)

    DateRepresentation.Yesterday -> stringResource(R.string.date_yesterday)

    DateRepresentation.Tomorrow -> stringResource(R.string.date_tomorrow)

    is DateRepresentation.DaysAgo -> pluralStringResource(
        R.plurals.date_days_ago,
        representation.days,
        representation.days,
    )

    DateRepresentation.LastWeek -> stringResource(R.string.date_last_week)

    is DateRepresentation.WeeksAgo -> pluralStringResource(
        R.plurals.date_weeks_ago,
        representation.weeks,
        representation.weeks,
    )

    DateRepresentation.LastMonth -> stringResource(R.string.date_last_month)

    is DateRepresentation.MonthsAgo -> pluralStringResource(
        R.plurals.date_months_ago,
        representation.months,
        representation.months,
    )

    is DateRepresentation.Custom -> rememberMediumDateLabel(representation.date)
}
