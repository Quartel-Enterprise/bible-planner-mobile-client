package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDateTime

fun interface LocalDateTimeProvider {
    fun getLocalDateTime(timestamp: Long): LocalDateTime
}
