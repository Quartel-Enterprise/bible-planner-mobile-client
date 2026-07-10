package com.quare.bibleplanner.feature.readingplan.presentation.content

import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel

private const val COLLAPSED_WEEK_LIMIT = 2

internal sealed interface ReadingPlanListItem {
    data class SectionHeader(
        val group: WeekGroup,
    ) : ReadingPlanListItem

    data class Week(
        val week: WeekPlanPresentationModel,
    ) : ReadingPlanListItem

    data class ShowMore(
        val group: WeekGroup,
        val totalWeeks: Int,
    ) : ReadingPlanListItem
}

internal const val SIDE_PANEL_ITEM_COUNT = 3

internal fun buildReadingPlanWeekItems(state: ReadingPlanUiState.Loaded): List<ReadingPlanListItem> {
    val grouped = state.weekPlans.groupBy { it.group }
    return buildList {
        addGroupSection(
            group = WeekGroup.Current,
            weeks = grouped[WeekGroup.Current].orEmpty(),
            isExpanded = true,
        )
        addGroupSection(
            group = WeekGroup.Upcoming,
            weeks = grouped[WeekGroup.Upcoming].orEmpty(),
            isExpanded = state.upcomingExpanded,
        )
        addGroupSection(
            group = WeekGroup.Completed,
            weeks = grouped[WeekGroup.Completed].orEmpty(),
            isExpanded = state.completedExpanded,
        )
    }
}

internal fun readingPlanWeekScrollIndex(
    state: ReadingPlanUiState.Loaded,
    includeSidePanel: Boolean,
    weekNumber: Int,
): Int {
    val weekItems = buildReadingPlanWeekItems(state)
    val indexInWeeks = weekItems.indexOfFirst { item ->
        item is ReadingPlanListItem.Week && item.week.weekPlan.number == weekNumber
    }
    if (indexInWeeks < 0) return -1
    val leading = if (includeSidePanel) SIDE_PANEL_ITEM_COUNT else 0
    return leading + indexInWeeks
}

internal fun readingPlanItemKey(item: ReadingPlanListItem): Any = when (item) {
    is ReadingPlanListItem.SectionHeader -> "section_${item.group}"
    is ReadingPlanListItem.Week -> item.week.weekPlan.number
    is ReadingPlanListItem.ShowMore -> "show_more_${item.group}"
}

private fun MutableList<ReadingPlanListItem>.addGroupSection(
    group: WeekGroup,
    weeks: List<WeekPlanPresentationModel>,
    isExpanded: Boolean,
) {
    if (weeks.isEmpty()) return
    add(ReadingPlanListItem.SectionHeader(group))
    val visibleWeeks = if (isExpanded) weeks else weeks.take(COLLAPSED_WEEK_LIMIT)
    visibleWeeks.forEach { week -> add(ReadingPlanListItem.Week(week)) }
    if (weeks.size > COLLAPSED_WEEK_LIMIT) {
        add(ReadingPlanListItem.ShowMore(group = group, totalWeeks = weeks.size))
    }
}
