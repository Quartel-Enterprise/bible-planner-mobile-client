package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import com.quare.bibleplanner.ui.component.ResponsiveColumn
import com.quare.bibleplanner.ui.component.ResponsiveContentScope
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
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
    ResponsiveColumn(
        lazyListState = lazyListState,
        contentPadding = mainPadding,
        maxContentWidth = 600.dp,
        portraitContent = {
            readingPlanContent(
                uiState = uiState,
                loadedUiState = loadedUiState,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        },
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun ResponsiveContentScope.readingPlanContent(
    uiState: ReadingPlanUiState,
    loadedUiState: ReadingPlanUiState.Loaded?,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    responsiveItem {
        PlanTypesSegmentedButtons(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            selectedReadingPlan = uiState.selectedReadingPlan,
            onPlanClick = {
                onEvent(ReadingPlanUiEvent.OnPlanClick(it))
            },
        )
    }
    responsiveItem {
        PlanProgress(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            progress = loadedUiState?.progress ?: 0f,
            isLoading = uiState is ReadingPlanUiState.Loading,
        )
    }
    item {
        VerticalSpacer()
    }
    when (uiState) {
        is ReadingPlanUiState.Loaded -> responsiveItems(
            items = uiState.weekPlans,
            key = { weekPresentation -> weekPresentation.weekPlan.number },
        ) { weekPresentation ->
            WeekPlanItem(
                modifier = Modifier.fillMaxWidth(),
                weekPresentation = weekPresentation,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onEvent = onEvent,
            )
        }

        is ReadingPlanUiState.Loading -> responsiveItem {
            LoadingReadingPlanContent(modifier = Modifier.fillMaxWidth())
        }
    }
}
