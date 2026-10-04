package com.quare.bibleplanner.feature.readingplan.screenshots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.fixture.readingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.content.ReadingPlanScreen
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotTest
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotVariant
import org.junit.Test

internal class ReadingPlanScreenshotTest : ScreenshotTest() {
    @Test
    fun loading() {
        snapshot(name = "loading") {
            ReadingPlanContent(
                uiState = ReadingPlanUiState.Loading(
                    selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
                    isShowingMenu = false,
                    isShowingOrderMenu = false,
                    scrollToWeekNumber = 0,
                    scrollToWeekIsAutomatic = false,
                    scrollToTop = false,
                    isScrolledDown = false,
                    isActiveRowVisible = true,
                ),
            )
        }
    }

    @Test
    fun onTrack() {
        snapshot(
            name = "on_track",
            variants = ScreenshotVariant.all,
        ) {
            ReadingPlanContent(uiState = readingPlanUiState())
        }
    }

    @Test
    fun onTrackLandscape() {
        snapshot(
            name = "on_track_landscape",
            isLandscape = true,
        ) {
            ReadingPlanContent(uiState = readingPlanUiState())
        }
    }

    @Test
    fun behind() {
        val uiState = readingPlanUiState()
        snapshot(
            name = "behind",
            variants = ScreenshotVariant.all,
        ) {
            ReadingPlanContent(
                uiState = uiState.copy(
                    planStatus = uiState.planStatus.copy(
                        mode = PlanMode.Behind,
                        daysBehind = DAYS_BEHIND,
                        daysSinceLastRead = DAYS_BEHIND,
                    ),
                ),
            )
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    private fun ReadingPlanContent(uiState: ReadingPlanUiState) {
        SharedTransitionLayout {
            AnimatedContent(targetState = Unit) {
                ReadingPlanScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    uiState = uiState,
                    onEvent = {},
                    lazyListState = rememberLazyListState(),
                )
            }
        }
    }

    private companion object {
        const val DAYS_BEHIND = 3
    }
}
