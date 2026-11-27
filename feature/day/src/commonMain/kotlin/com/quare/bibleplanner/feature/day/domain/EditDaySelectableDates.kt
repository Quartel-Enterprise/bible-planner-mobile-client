package com.quare.bibleplanner.feature.day.domain

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.date.toLocalDate
import com.quare.bibleplanner.core.date.toTimestampUTC
import kotlinx.datetime.LocalDate

// Why: plain Kotlin so SwiftUI can ask it too; the Compose date picker wraps it in a SelectableDates.
class EditDaySelectableDates(
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= getToday().toTimestampUTC()

    fun isSelectableYear(year: Int): Boolean = year <= getToday().year

    private fun getToday(): LocalDate = localDateTimeProvider
        .getLocalDateTime(currentTimestampProvider.getCurrentTimestamp())
        .toLocalDate()
}
