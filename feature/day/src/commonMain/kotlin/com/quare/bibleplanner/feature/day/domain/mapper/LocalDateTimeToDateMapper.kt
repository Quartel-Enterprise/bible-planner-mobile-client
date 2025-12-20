package com.quare.bibleplanner.feature.day.domain.mapper

import com.quare.bibleplanner.core.model.date.DateModel
import kotlinx.datetime.LocalDateTime

class LocalDateTimeToDateMapper {
    fun map(localDateTime: LocalDateTime): DateModel = localDateTime.run {
        DateModel(
            day = day,
            month = month,
            year = year,
            second = second,
            minute = minute,
            hour = hour,
        )
    }
}
