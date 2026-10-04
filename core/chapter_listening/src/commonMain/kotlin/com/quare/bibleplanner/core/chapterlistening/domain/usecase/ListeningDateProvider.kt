package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import kotlinx.datetime.LocalDate

class ListeningDateProvider(
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    val today: LocalDate
        get() = localDateTimeProvider
            .getLocalDateTime(currentTimestampProvider.getCurrentTimestamp())
            .date
}
