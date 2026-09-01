package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

internal class DeleteProgressMapper {
    fun map(state: ReadingPlanUiState): Boolean? = when (state) {
        is ReadingPlanUiState.Loaded -> state.weekPlans.containsReadDay()
        is ReadingPlanUiState.Loading -> null
    }

    private fun List<WeekPlanPresentationModel>.containsReadDay(): Boolean = any {
        it.weekPlan.days.any { day -> day.isRead }
    }
}
