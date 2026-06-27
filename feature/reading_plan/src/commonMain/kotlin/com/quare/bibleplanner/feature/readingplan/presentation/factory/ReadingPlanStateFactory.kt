package com.quare.bibleplanner.feature.readingplan.presentation.factory

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState

internal class ReadingPlanStateFactory {
    private val defaultPlan = ReadingPlanType.CHRONOLOGICAL

    fun createFirstState(): ReadingPlanUiState.Loading = ReadingPlanUiState.Loading(
        selectedReadingPlan = defaultPlan,
        isShowingMenu = false,
        scrollToWeekNumber = 0,
        scrollToWeekIsAutomatic = false,
        scrollToTop = false,
        isScrolledDown = false,
        isActiveRowVisible = true,
    )
}
