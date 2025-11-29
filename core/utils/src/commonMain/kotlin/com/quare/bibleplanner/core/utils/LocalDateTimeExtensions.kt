package com.quare.bibleplanner.core.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Converts a LocalDateTime to a LocalDate by extracting the date components.
 */
fun LocalDateTime.toLocalDate(): LocalDate = LocalDate(
    year = year,
    month = month,
    day = day,
)
