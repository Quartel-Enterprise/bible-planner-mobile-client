package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class LocalDateTimeProvider {
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()

    fun getLocalDateTime(timestamp: Long): LocalDateTime = Instant
        .fromEpochMilliseconds(timestamp)
        .toLocalDateTime(timeZone)
}
