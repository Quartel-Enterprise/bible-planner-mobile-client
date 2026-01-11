package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.Composable
import bibleplanner.ui.utils.generated.resources.Res
import bibleplanner.ui.utils.generated.resources.date_days_ago
import bibleplanner.ui.utils.generated.resources.date_last_month
import bibleplanner.ui.utils.generated.resources.date_last_week
import bibleplanner.ui.utils.generated.resources.date_months_ago
import bibleplanner.ui.utils.generated.resources.date_today
import bibleplanner.ui.utils.generated.resources.date_tomorrow
import bibleplanner.ui.utils.generated.resources.date_weeks_ago
import bibleplanner.ui.utils.generated.resources.date_yesterday
import com.quare.bibleplanner.core.date.DateRepresentation
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DateRepresentation.format(): String = when (this) {
    DateRepresentation.Today -> {
        stringResource(Res.string.date_today)
    }

    DateRepresentation.Yesterday -> {
        stringResource(Res.string.date_yesterday)
    }

    DateRepresentation.Tomorrow -> {
        stringResource(Res.string.date_tomorrow)
    }

    is DateRepresentation.DaysAgo -> {
        pluralStringResource(Res.plurals.date_days_ago, days, days)
    }

    DateRepresentation.LastWeek -> {
        stringResource(Res.string.date_last_week)
    }

    is DateRepresentation.WeeksAgo -> {
        pluralStringResource(Res.plurals.date_weeks_ago, weeks, weeks)
    }

    DateRepresentation.LastMonth -> {
        stringResource(Res.string.date_last_month)
    }

    is DateRepresentation.MonthsAgo -> {
        pluralStringResource(Res.plurals.date_months_ago, months, months)
    }

    is DateRepresentation.Custom -> {
        val monthStr = stringResource(date.month.toStringResource())
            .lowercase()
            .replaceFirstChar { it.uppercase() }
            .take(3)
        "$monthStr ${date.day}, ${date.year}"
    }
}
