package com.quare.bibleplanner.core.chapterlistening.domain.model

import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel

data class ListeningDayModel(
    val location: PlanDayLocationModel,
    val segments: List<ListeningSegmentModel>,
)
