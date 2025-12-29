package com.quare.bibleplanner.feature.day.presentation.mapper

import com.quare.bibleplanner.core.model.date.DateModel
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.feature.day.domain.mapper.LocalDateTimeToDateMapper
import com.quare.bibleplanner.ui.component.date.DatePresentationModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ReadDateFormatter(
    private val localDateTimeToDateMapper: LocalDateTimeToDateMapper,
    private val monthPresentationMapper: MonthPresentationMapper,
    private val localDateTimeProvider: LocalDateTimeProvider,
) {
    fun format(timestamp: Long): DatePresentationModel = localDateTimeToDateMapper
        .map(
            localDateTime = localDateTimeProvider.getLocalDateTime(timestamp),
        ).toPresentation()

    private fun DateModel.toPresentation(): DatePresentationModel = DatePresentationModel(
        minute = minute.toString().padStart(2, '0'),
        hour = hour.toString().padStart(2, '0'),
        day = day,
        month = monthPresentationMapper.map(month),
        year = year,
    )
}
