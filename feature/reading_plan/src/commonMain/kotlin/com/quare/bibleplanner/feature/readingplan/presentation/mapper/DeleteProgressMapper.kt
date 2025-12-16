package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiAction
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

internal class DeleteProgressMapper {
    fun map(state: ReadingPlanUiState): ReadingPlanUiAction? = when (state) {
        is ReadingPlanUiState.Loaded -> if (state.weekPlans.containsReadDay()) {
            ReadingPlanUiAction.GoToDeleteAllProgress
        } else {
            ReadingPlanUiAction.ShowNoProgressToDelete
        }

        is ReadingPlanUiState.Loading -> null
    }

    private fun List<WeekPlanPresentationModel>.containsReadDay(): Boolean = any {
        it.weekPlan.days.any { day -> day.isRead }
    }
}
