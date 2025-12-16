package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_title_part
import bibleplanner.feature.day.generated.resources.week_title_part
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.icon.BackIcon
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayScreenTopBarComponent(
    uiState: DayUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (DayUiEvent) -> Unit,
) {
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
                        completedCount = completedPassagesCount,
                        totalCount = totalPassagesCount,
                    )
                }
            }
        },
        navigationIcon = {
            BackIcon(onBackClick = { onEvent(DayUiEvent.OnBackClick) })
        },
        scrollBehavior = scrollBehavior,
    )
}
