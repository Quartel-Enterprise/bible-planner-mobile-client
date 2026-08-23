package com.quare.bibleplanner.core.model.plan

import kotlinx.datetime.LocalDate

data class ScheduledDayModel(
    val number: Int,
    val passages: List<PassageModel>,
    val plannedReadDate: LocalDate?,
)
