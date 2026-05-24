package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanProgress
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanTypesSegmentedButtons
import com.quare.bibleplanner.feature.readingplan.presentation.component.week.WeekPlanItem
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.ResponsiveSplitColumn
import com.quare.bibleplanner.ui.utils.LocalMainPadding

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
    val readDaysCount = loadedUiState?.readDaysCount ?: 0
    val totalDaysCount = loadedUiState?.totalDaysCount ?: 0
    ResponsiveSplitColumn(
        lazyListState = lazyListState,
        contentPadding = mainPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalSpacing = 8.dp,
        portraitContent = {
            sidePanelItems(
                uiState = uiState,
                loadedUiState = loadedUiState,
                onEvent = onEvent,
                readDaysCount = readDaysCount,
                totalDaysCount = totalDaysCount,
            )
            weeksItems(
                uiState = uiState,
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
                readDaysCount = readDaysCount,
                totalDaysCount = totalDaysCount,
            )
        },
        landscapeRightContent = {
            weeksItems(
                uiState = uiState,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 16.dp),
            )
        },
    )
}

private fun ResponsiveContentScope.sidePanelItems(
    uiState: ReadingPlanUiState,
    loadedUiState: ReadingPlanUiState.Loaded?,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    readDaysCount: Int,
    totalDaysCount: Int,
    modifier: Modifier = Modifier,
) {
    responsiveItem(key = "segmented") {
        PlanTypesSegmentedButtons(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            selectedReadingPlan = uiState.selectedReadingPlan,
            onPlanClick = {
                onEvent(ReadingPlanUiEvent.OnPlanClick(it))
            },
        )
    }
    responsiveItem(key = "progress") {
        PlanProgress(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            progress = loadedUiState?.progress ?: 0f,
            readDaysCount = readDaysCount,
            totalDaysCount = totalDaysCount,
            isLoading = uiState is ReadingPlanUiState.Loading,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun ResponsiveContentScope.weeksItems(
    uiState: ReadingPlanUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is ReadingPlanUiState.Loaded -> responsiveItems(
            items = uiState.weekPlans,
            key = { weekPresentation -> weekPresentation.weekPlan.number },
        ) { weekPresentation ->
            WeekPlanItem(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                weekPresentation = weekPresentation,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onEvent = onEvent,
            )
        }

        is ReadingPlanUiState.Loading -> responsiveItem(key = "loading") {
            LoadingReadingPlanContent(modifier = Modifier.fillMaxWidth())
        }
    }
}
