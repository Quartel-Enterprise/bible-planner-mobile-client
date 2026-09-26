package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal class LocalDateTimeProviderImpl : LocalDateTimeProvider {
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()

    override fun getLocalDateTime(timestamp: Long): LocalDateTime = Instant
        .fromEpochMilliseconds(timestamp)
        .toLocalDateTime(timeZone)
}
