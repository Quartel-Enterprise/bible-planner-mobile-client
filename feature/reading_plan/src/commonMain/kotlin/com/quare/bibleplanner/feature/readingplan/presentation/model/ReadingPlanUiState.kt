package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage

internal sealed interface ReadingPlanUiState {
    val selectedReadingPlan: ReadingPlanType
    val isShowingMenu: Boolean
    val scrollToWeekNumber: Int
    val scrollToTop: Boolean
    val isScrolledDown: Boolean
    val isFirstUnreadWeekVisible: Boolean

    data class Loaded(
        val weekPlans: List<WeekPlanPresentationModel>,
        val progress: Float,
        val motivationMessage: PlanMotivationMessage,
        override val selectedReadingPlan: ReadingPlanType,
        override val isShowingMenu: Boolean,
        override val scrollToWeekNumber: Int = 0,
        override val scrollToTop: Boolean = false,
        override val isScrolledDown: Boolean = false,
        override val isFirstUnreadWeekVisible: Boolean = false,
    ) : ReadingPlanUiState {
        val readDaysCount: Int = weekPlans.sumOf { it.readDaysCount }
        val totalDaysCount: Int = weekPlans.sumOf { it.totalDays }
    }

    data class Loading(
        override val selectedReadingPlan: ReadingPlanType,
        override val isShowingMenu: Boolean,
        override val scrollToWeekNumber: Int = 0,
        override val scrollToTop: Boolean = false,
        override val isScrolledDown: Boolean = false,
        override val isFirstUnreadWeekVisible: Boolean = false,
    ) : ReadingPlanUiState
}
