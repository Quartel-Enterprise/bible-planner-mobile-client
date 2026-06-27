package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus

internal sealed interface ReadingPlanUiState {
    val selectedReadingPlan: ReadingPlanType
    val isShowingMenu: Boolean
    val scrollToWeekNumber: Int
    val scrollToWeekIsAutomatic: Boolean
    val scrollToTop: Boolean
    val isScrolledDown: Boolean
    val isActiveRowVisible: Boolean

    data class Loaded(
        val weekPlans: List<WeekPlanPresentationModel>,
        val progress: Float,
        val motivationMessage: PlanMotivationMessage,
        val planStatus: PlanStatus,
        val upcomingExpanded: Boolean,
        val completedExpanded: Boolean,
        val flashTargetGlobalIndex: Int,
        override val selectedReadingPlan: ReadingPlanType,
        override val isShowingMenu: Boolean,
        override val scrollToWeekNumber: Int,
        override val scrollToWeekIsAutomatic: Boolean,
        override val scrollToTop: Boolean,
        override val isScrolledDown: Boolean,
        override val isActiveRowVisible: Boolean,
    ) : ReadingPlanUiState {
        val readDaysCount: Int = weekPlans.sumOf { it.readDaysCount }
        val totalDaysCount: Int = weekPlans.sumOf { it.totalDays }
    }

    data class Loading(
        override val selectedReadingPlan: ReadingPlanType,
        override val isShowingMenu: Boolean,
        override val scrollToWeekNumber: Int,
        override val scrollToWeekIsAutomatic: Boolean,
        override val scrollToTop: Boolean,
        override val isScrolledDown: Boolean,
        override val isActiveRowVisible: Boolean,
    ) : ReadingPlanUiState
}
