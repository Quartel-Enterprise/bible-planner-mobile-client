package com.quare.bibleplanner.core.daystudy.domain.model

import com.quare.bibleplanner.core.model.route.DayNavRoute

data class DayStudyGenerationJob(
    val key: String,
    val label: String,
    val dayRoute: DayNavRoute,
    val phase: DayStudyPhaseModel?,
    val status: DayStudyGenerationStatus,
)
