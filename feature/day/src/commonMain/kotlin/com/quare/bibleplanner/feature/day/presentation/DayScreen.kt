package com.quare.bibleplanner.feature.day.presentation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_title_part
import bibleplanner.feature.day.generated.resources.week_title_part
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.day.presentation.component.DayProgress
import com.quare.bibleplanner.feature.day.presentation.content.DayContent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

private const val MAX_CONTENT_WIDTH = 600

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayScreen(
    uiState: DayUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onEvent: (DayUiEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    (uiState as? DayUiState.Loaded)?.run {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                with(sharedTransitionScope) {
                                    Text(
                                        text = stringResource(
                                            Res.string.day_title_part,
                                            day.number,
                                        ),
                                        modifier = Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                key = SharedTransitionAnimationUtils.buildDayNumberId(
                                                    weekNumber = weekNumber,
                                                    dayNumebr = day.number,
                                                ),
                                            ),
                                            animatedVisibilityScope = animatedContentScope,
                                        ),
                                    )
                                    Text(
                                        modifier = Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                key = SharedTransitionAnimationUtils.buildWeekSeparatorId(weekNumber),
                                            ),
                                            animatedVisibilityScope = animatedContentScope,
                                        ),
                                        text = " — ",
                                    )
                                    Text(
                                        text = stringResource(
                                            Res.string.week_title_part,
                                            weekNumber,
                                        ),
                                        modifier = Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                key = SharedTransitionAnimationUtils.buildWeekNumberId(
                                                    weekNumber = weekNumber,
                                                ),
                                            ),
                                            animatedVisibilityScope = animatedContentScope,
                                        ),
                                    )
                                }
                            }
                            VerticalSpacer(4)
                            DayProgress(
                                passages = day.passages,
                                books = books,
                            )
                        }
                    }
                },
                navigationIcon = {
                    BackIcon(onBackClick = { onEvent(DayUiEvent.OnBackClick) })
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val constrainedWidth = maxWidth.coerceAtMost(MAX_CONTENT_WIDTH.dp)
            DayContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onEvent = onEvent,
                maxContentWidth = constrainedWidth,
            )
        }
    }
}
