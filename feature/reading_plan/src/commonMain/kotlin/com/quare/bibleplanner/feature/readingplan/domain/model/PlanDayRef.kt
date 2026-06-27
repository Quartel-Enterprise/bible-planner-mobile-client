package com.quare.bibleplanner.feature.readingplan.domain.model

import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlinx.datetime.LocalDate

internal data class PlanDayRef(
    val weekNumber: Int,
    val dayNumber: Int,
    val globalIndex: Int,
    val passages: List<PassageModel>,
    val plannedReadDate: LocalDate?,
)
