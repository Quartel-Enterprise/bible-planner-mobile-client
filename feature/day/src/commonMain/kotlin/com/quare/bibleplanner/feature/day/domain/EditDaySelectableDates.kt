package com.quare.bibleplanner.feature.day.domain

import com.quare.bibleplanner.core.date.toTimestampUTC
import kotlinx.datetime.LocalDate

// Why: a plain value so every UI (Compose wraps it in a SelectableDates, SwiftUI reads the date) applies the same rule.
data class EditDaySelectableDates(
    val latestSelectableDate: LocalDate,
) {
    fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= latestSelectableDate.toTimestampUTC()

    fun isSelectableYear(year: Int): Boolean = year <= latestSelectableDate.year
}
