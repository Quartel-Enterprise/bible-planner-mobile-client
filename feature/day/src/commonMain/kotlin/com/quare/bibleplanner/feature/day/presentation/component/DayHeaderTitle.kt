package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_title_part
import bibleplanner.feature.day.generated.resources.week_title_part
import com.quare.bibleplanner.core.plan.domain.getGlobalDayIndex
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayHeaderTitle(
    state: DayUiState.Loaded,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            with(sharedTransitionScope) {
                Text(
                    text = stringResource(
                        Res.string.week_title_part,
                        state.weekNumber,
                    ),
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = SharedTransitionAnimationUtils.buildWeekNumberId(
                                weekNumber = state.weekNumber,
                            ),
                        ),
                        animatedVisibilityScope = animatedContentScope,
                    ),
                )
                Text(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = SharedTransitionAnimationUtils.buildWeekSeparatorId(state.weekNumber),
                        ),
                        animatedVisibilityScope = animatedContentScope,
                    ),
                    text = " — ",
                )
                Text(
                    text = stringResource(
                        Res.string.day_title_part,
                        getGlobalDayIndex(
                            weekNumber = state.weekNumber,
                            dayNumber = state.day.number,
                        ),
                    ),
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = SharedTransitionAnimationUtils.buildDayNumberId(
                                weekNumber = state.weekNumber,
                                dayNumebr = state.day.number,
                            ),
                        ),
                        animatedVisibilityScope = animatedContentScope,
                    ),
                )
            }
        }
        VerticalSpacer(4)
        DayProgress(
            completedCount = state.completedPassagesCount,
            totalCount = state.totalPassagesCount,
        )
    }
}
