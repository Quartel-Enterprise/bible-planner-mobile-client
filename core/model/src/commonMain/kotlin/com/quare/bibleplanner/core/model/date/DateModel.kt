package com.quare.bibleplanner.core.model.date

import kotlinx.datetime.Month

data class DateModel(
    val day: Int,
    val month: Month,
    val year: Int,
    val second: Int,
    val minute: Int,
    val hour: Int,
)
