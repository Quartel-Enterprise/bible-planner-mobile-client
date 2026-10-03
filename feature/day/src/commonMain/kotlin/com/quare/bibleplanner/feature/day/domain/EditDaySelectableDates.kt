package com.quare.bibleplanner.feature.day.domain

import androidx.compose.material3.SelectableDates
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.date.toLocalDate
import com.quare.bibleplanner.core.date.toTimestampUTC
import kotlinx.datetime.LocalDate

class EditDaySelectableDates(
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= getToday().toTimestampUTC()

    override fun isSelectableYear(year: Int): Boolean = year <= getToday().year

    private fun getToday(): LocalDate = localDateTimeProvider
        .getLocalDateTime(currentTimestampProvider.getCurrentTimestamp())
        .toLocalDate()
}
