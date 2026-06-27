package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.section_completed
import bibleplanner.feature.reading_plan.generated.resources.section_current
import bibleplanner.feature.reading_plan.generated.resources.section_current_behind
import bibleplanner.feature.reading_plan.generated.resources.section_upcoming
import bibleplanner.feature.reading_plan.generated.resources.see_less
import bibleplanner.feature.reading_plan.generated.resources.see_more_weeks
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanProgress
import com.quare.bibleplanner.feature.readingplan.presentation.component.ReadingPlanHeaderRow
import com.quare.bibleplanner.feature.readingplan.presentation.component.hero.PlanHeroCard
import com.quare.bibleplanner.feature.readingplan.presentation.component.shimmers.PlanHeroShimmer
import com.quare.bibleplanner.feature.readingplan.presentation.component.shimmers.PlanProgressShimmer
import com.quare.bibleplanner.feature.readingplan.presentation.component.shimmers.SectionHeaderShimmer
import com.quare.bibleplanner.feature.readingplan.presentation.component.shimmers.WeekShimmerCard
import com.quare.bibleplanner.feature.readingplan.presentation.component.week.WeekPlanItem
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
import com.quare.bibleplanner.feature.readingplan.presentation.utils.ScrollToWeekAction
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.ResponsiveSplitColumn
import com.quare.bibleplanner.ui.utils.LocalMainPadding
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val SPLIT_BREAKPOINT = 600.dp
private const val WEEK_SHIMMER_COUNT = 4

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ReadingPlanScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    lazyListState: LazyListState,
) {
    val loadedUiState = uiState as? ReadingPlanUiState.Loaded
    val mainPadding = LocalMainPadding.current
    BoxWithConstraints {
        val includeSidePanel = maxWidth <= SPLIT_BREAKPOINT
        ScrollToWeekAction(
            scrollToWeekNumber = uiState.scrollToWeekNumber,
            scrollToWeekIsAutomatic = uiState.scrollToWeekIsAutomatic,
            loadedUiState = loadedUiState,
            includeSidePanel = includeSidePanel,
            lazyListState = lazyListState,
            onEvent = onEvent,
        )
        ResponsiveSplitColumn(
            maxContentWidth = 1000.dp,
            lazyListState = lazyListState,
            contentPadding = mainPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalSpacing = 8.dp,
            portraitContent = {
                sidePanelItems(
                    uiState = uiState,
                    loadedUiState = loadedUiState,
                    onEvent = onEvent,
                )
                weekItems(
                    loadedUiState = loadedUiState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    onEvent = onEvent,
                )
            },
            landscapeLeftContent = {
                sidePanelItems(
                    uiState = uiState,
                    loadedUiState = loadedUiState,
                    onEvent = onEvent,
                )
            },
            landscapeRightContent = {
                weekItems(
                    loadedUiState = loadedUiState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    onEvent = onEvent,
                )
            },
        )
    }
}

private fun ResponsiveContentScope.sidePanelItems(
    uiState: ReadingPlanUiState,
    loadedUiState: ReadingPlanUiState.Loaded?,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    responsiveItem(key = "header") {
        ReadingPlanHeaderRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            selectedReadingPlan = uiState.selectedReadingPlan,
            isShowingMenu = uiState.isShowingMenu,
            onEvent = onEvent,
        )
    }
    val sectionModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    responsiveItem(key = "hero") {
        if (loadedUiState != null) {
            PlanHeroCard(
                modifier = sectionModifier,
                planStatus = loadedUiState.planStatus,
                onEvent = onEvent,
            )
        } else {
            PlanHeroShimmer(modifier = sectionModifier)
        }
    }
    responsiveItem(key = "progress") {
        if (loadedUiState != null) {
            PlanProgress(
                modifier = sectionModifier,
                progress = loadedUiState.progress,
                readDaysCount = loadedUiState.readDaysCount,
                totalDaysCount = loadedUiState.totalDaysCount,
                motivationMessage = loadedUiState.motivationMessage,
                planStatus = loadedUiState.planStatus,
            )
        } else {
            PlanProgressShimmer(modifier = sectionModifier)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun ResponsiveContentScope.weekItems(
    loadedUiState: ReadingPlanUiState.Loaded?,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEvent: (ReadingPlanUiEvent) -> Unit,
) {
    if (loadedUiState == null) {
        responsiveItem(key = "week_shimmer_section_header") {
            SectionHeaderShimmer()
        }
        repeat(WEEK_SHIMMER_COUNT) { index ->
            responsiveItem(key = "week_shimmer_$index") {
                WeekShimmerCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    isCurrent = index == 0,
                )
            }
        }
        return
    }
    val items = buildReadingPlanWeekItems(loadedUiState)
    responsiveItems(
        items = items,
        key = { item -> readingPlanItemKey(item) },
        animateItem = true,
    ) { item ->
        when (item) {
            is ReadingPlanListItem.SectionHeader -> SectionHeaderRow(
                group = item.group,
                isBehind = loadedUiState.planStatus.mode == PlanMode.Behind,
            )

            is ReadingPlanListItem.Week -> WeekPlanItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                weekPresentation = item.week,
                flashTargetGlobalIndex = loadedUiState.flashTargetGlobalIndex,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onEvent = onEvent,
            )

            is ReadingPlanListItem.ShowMore -> ShowMoreButton(
                group = item.group,
                totalWeeks = item.totalWeeks,
                isExpanded = item.group.isExpanded(loadedUiState),
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun SectionHeaderRow(
    group: WeekGroup,
    isBehind: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        text = stringResource(group.sectionResource(isBehind = isBehind)),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = if (group == WeekGroup.Current) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

@Composable
private fun ShowMoreButton(
    group: WeekGroup,
    totalWeeks: Int,
    isExpanded: Boolean,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hiddenCount = (totalWeeks - 2).coerceAtLeast(0)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        TextButton(onClick = { onEvent(group.toToggleEvent()) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (isExpanded) {
                        stringResource(Res.string.see_less)
                    } else {
                        pluralStringResource(Res.plurals.see_more_weeks, hiddenCount, hiddenCount)
                    },
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            }
        }
    }
}

private fun WeekGroup.sectionResource(isBehind: Boolean): StringResource = when (this) {
    WeekGroup.Current -> if (isBehind) Res.string.section_current_behind else Res.string.section_current
    WeekGroup.Upcoming -> Res.string.section_upcoming
    WeekGroup.Completed -> Res.string.section_completed
}

private fun WeekGroup.isExpanded(state: ReadingPlanUiState.Loaded): Boolean = when (this) {
    WeekGroup.Upcoming -> state.upcomingExpanded
    WeekGroup.Completed -> state.completedExpanded
    WeekGroup.Current -> true
}

private fun WeekGroup.toToggleEvent(): ReadingPlanUiEvent = when (this) {
    WeekGroup.Completed -> ReadingPlanUiEvent.OnToggleCompletedExpanded
    else -> ReadingPlanUiEvent.OnToggleUpcomingExpanded
}
