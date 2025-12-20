package com.quare.bibleplanner.feature.day.presentation.mapper

import com.quare.bibleplanner.core.model.date.DateModel
import com.quare.bibleplanner.feature.day.domain.mapper.LocalDateTimeToDateMapper
import com.quare.bibleplanner.ui.component.date.DatePresentationModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal class ReadDateFormatter(
    private val localDateTimeToDateMapper: LocalDateTimeToDateMapper,
    private val monthPresentationMapper: MonthPresentationMapper,
) {
    fun format(timestamp: Long): DatePresentationModel {
        val localDateTime: LocalDateTime = Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val domainModel = localDateTimeToDateMapper.map(localDateTime)
        return domainModel.toPresentation()
    }

    private fun DateModel.toPresentation(): DatePresentationModel = DatePresentationModel(
        minute = minute.toString().padStart(2, '0'),
        hour = hour.toString().padStart(2, '0'),
        day = day,
        month = monthPresentationMapper.map(month),
        year = year,
    )
}
