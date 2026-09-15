package com.quare.bibleplanner.feature.day.domain

import androidx.compose.material3.SelectableDates
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class EditDaySelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        // Block future dates - only allow dates up to now
        val now = Clock.System.now().toEpochMilliseconds()
        return utcTimeMillis <= now
    }

    override fun isSelectableYear(year: Int): Boolean {
        // Get current year from current timestamp
        val now = Clock.System.now().toEpochMilliseconds()
        val currentYear = Instant
            .fromEpochMilliseconds(now)
            .toLocalDateTime(TimeZone.UTC)
            .year
        return year <= currentYear
    }
}
