package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanProgress
import com.quare.bibleplanner.feature.readingplan.presentation.component.PlanTypesSegmentedButtons
import com.quare.bibleplanner.feature.readingplan.presentation.component.week.WeekPlanItem
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ReadingPlanContent(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    uiState: ReadingPlanUiState,
    onEvent: (ReadingPlanUiEvent) -> Unit,
    maxContentWidth: Dp,
    lazyListState: LazyListState,
) {
    val loadedUiState = uiState as? ReadingPlanUiState.Loaded

    BoxWithConstraints(modifier = modifier) {
        val isLandscape = maxWidth > maxHeight

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.width(maxContentWidth)) {
                        PlanTypesSegmentedButtons(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            selectedReadingPlan = uiState.selectedReadingPlan,
                            onPlanClick = {
                                onEvent(ReadingPlanUiEvent.OnPlanClick(it))
                            },
                        )
                    }
                }
                VerticalSpacer()
            }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.width(maxContentWidth)) {
                        PlanProgress(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            progress = loadedUiState?.progress ?: 0f,
                            isLoading = uiState is ReadingPlanUiState.Loading,
                        )
                    }
                }
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
                        Box(modifier = Modifier.width(maxContentWidth)) {
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

                is ReadingPlanUiState.Loading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.width(maxContentWidth)) {
                            LoadingReadingPlanContent(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            // Add bottom padding to prevent FABs from covering the last items (only in portrait)
            if (!isLandscape) {
                item {
                    VerticalSpacer(size = 140.dp)
                }
            }
        }
    }
}
