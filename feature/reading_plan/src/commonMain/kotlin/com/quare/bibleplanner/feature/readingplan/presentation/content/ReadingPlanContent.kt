package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.component.ResponsiveContent
import com.quare.bibleplanner.ui.component.centeredItem
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanProgress
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanTypesSegmentedButtons
import com.quare.bibleplanner.feature.readingplan.presentation.component.week.WeekPlanItem
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

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

    ResponsiveContent(
        lazyListState = lazyListState,
        maxContentWidth = 600.dp,
        portraitContent = { contentMaxWidth ->
            readingPlanContent(
                uiState = uiState,
                loadedUiState = loadedUiState,
                contentMaxWidth = contentMaxWidth,
                onEvent = onEvent,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        }
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
private fun LazyListScope.readingPlanContent(
    uiState: ReadingPlanUiState,
    loadedUiState: ReadingPlanUiState.Loaded?,
    contentMaxWidth: Dp,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    centeredItem(contentMaxWidth) {
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
    centeredItem(contentMaxWidth) {
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
        is ReadingPlanUiState.Loaded -> items(
            items = uiState.weekPlans,
            key = { weekPresentation -> weekPresentation.weekPlan.number },
        ) { weekPresentation ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(contentMaxWidth)) {
                    WeekPlanItem(
                        modifier = Modifier.fillMaxWidth(),
                        weekPresentation = weekPresentation,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope,
                        onEvent = onEvent,
                    )
                }
            }
        }

        is ReadingPlanUiState.Loading -> centeredItem(contentMaxWidth) {
            LoadingReadingPlanContent(modifier = Modifier.fillMaxWidth())
        }
    }
}
