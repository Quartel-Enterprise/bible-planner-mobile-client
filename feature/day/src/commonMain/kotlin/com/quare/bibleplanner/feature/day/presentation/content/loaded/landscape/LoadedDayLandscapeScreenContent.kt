package com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.presentation.component.DayHeaderTitle
import com.quare.bibleplanner.feature.day.presentation.component.DayLandscapeHeader
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side.LoadedDayLandscapeScreenLeftContent
import com.quare.bibleplanner.feature.day.presentation.content.loaded.landscape.side.LoadedDayLandscapeScreenRightContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState

private const val READING_WEIGHT = 2f
private const val STUDY_WEIGHT = 3f
private val ReadingContentMaxWidth = 560.dp
private val StudyContentMaxWidth = 560.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LoadedDayLandscapeContent(
    day: DayModel,
    uiState: DayUiState.Loaded,
    onEvent: (DayUiEvent) -> Unit,
    platform: Platform,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(READING_WEIGHT)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ReadingContentMaxWidth)
                    .fillMaxHeight(),
            ) {
                DayLandscapeHeader(
                    platform = platform,
                    onBackClick = { onEvent(DayUiEvent.OnBackClick) },
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    DayHeaderTitle(
                        state = uiState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope,
                        horizontalAlignment = Alignment.Start,
                    )
                }
                LoadedDayLandscapeScreenLeftContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = 16.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 24.dp,
                        ),
                    day = day,
                    uiState = uiState,
                    onEvent = onEvent,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                )
            }
        }
        VerticalDivider()
        Box(
            modifier = Modifier
                .weight(STUDY_WEIGHT)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LoadedDayLandscapeScreenRightContent(
                modifier = Modifier
                    .widthIn(max = StudyContentMaxWidth)
                    .fillMaxSize(),
                day = day,
                dayRoute = uiState.dayRoute,
                onEvent = onEvent,
            )
        }
    }
}
