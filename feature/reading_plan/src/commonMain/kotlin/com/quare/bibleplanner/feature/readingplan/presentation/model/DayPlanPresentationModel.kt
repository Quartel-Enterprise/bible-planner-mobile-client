package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.DayModel

data class DayPlanPresentationModel(
    val day: DayModel,
    val globalDayIndex: Int,
    val shouldShowYear: Boolean,
    val isNextToRead: Boolean,
    val isOverdue: Boolean,
    val isActive: Boolean,
    val isAccented: Boolean,
)
