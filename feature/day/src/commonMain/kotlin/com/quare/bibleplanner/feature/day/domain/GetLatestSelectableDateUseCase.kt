package com.quare.bibleplanner.feature.day.domain

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.date.toLocalDate
import kotlinx.datetime.LocalDate

internal class GetLatestSelectableDateUseCase(
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    operator fun invoke(): LocalDate = localDateTimeProvider
        .getLocalDateTime(currentTimestampProvider.getCurrentTimestamp())
        .toLocalDate()
}
