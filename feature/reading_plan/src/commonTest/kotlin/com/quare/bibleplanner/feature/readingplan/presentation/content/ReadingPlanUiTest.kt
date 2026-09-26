package com.quare.bibleplanner.feature.readingplan.presentation.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.day_number
import bibleplanner.feature.reading_plan.generated.resources.hero_day_of_total
import bibleplanner.feature.reading_plan.generated.resources.hero_kicker_today
import bibleplanner.feature.reading_plan.generated.resources.hero_primary_read_now
import bibleplanner.feature.reading_plan.generated.resources.mark_as_read
import bibleplanner.feature.reading_plan.generated.resources.more_options
import bibleplanner.feature.reading_plan.generated.resources.week_number
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.fixture.readingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiEvent
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class ReadingPlanUiTest {
    private val loadingUiState = ReadingPlanUiState.Loading(
        selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
        isShowingMenu = false,
        isShowingOrderMenu = false,
        scrollToWeekNumber = 0,
        scrollToWeekIsAutomatic = false,
        scrollToTop = false,
        isScrolledDown = false,
        isActiveRowVisible = true,
    )
    private lateinit var events: MutableList<ReadingPlanUiEvent>

    @Test
    fun `GIVEN a loaded plan WHEN rendered THEN the hero shows the reading of today`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = readingPlanUiState())

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(Res.string.hero_kicker_today)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.hero_day_of_total, TODAY_GLOBAL_INDEX, TOTAL_DAYS)).assertIsDisplayed()
    }

    @Test
    fun `GIVEN a loaded plan WHEN clicking read now THEN emits OnDayClick for the next day`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = readingPlanUiState())

        // When
        onNodeWithText(getString(Res.string.hero_primary_read_now)).performClick()

        // Then
        assertEquals(
            expected = listOf<ReadingPlanUiEvent>(
                ReadingPlanUiEvent.OnDayClick(dayNumber = TODAY_DAY_NUMBER, weekNumber = CURRENT_WEEK),
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a loaded plan WHEN clicking mark as read THEN emits OnDayReadClick for the next day`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = readingPlanUiState())

            // When
            onNodeWithContentDescription(getString(Res.string.mark_as_read)).performClick()

            // Then
            assertEquals(
                expected = listOf<ReadingPlanUiEvent>(
                    ReadingPlanUiEvent.OnDayReadClick(dayNumber = TODAY_DAY_NUMBER, weekNumber = CURRENT_WEEK),
                ),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a loaded plan WHEN clicking a day of the current week THEN emits OnDayClick for it`() =
        runComposeUiTest {
            // Given
            prepareScenario(uiState = readingPlanUiState())

            // When
            onNodeWithText(getString(Res.string.day_number, FIRST_DAY_GLOBAL_INDEX)).performClick()

            // Then
            assertEquals(
                expected = listOf<ReadingPlanUiEvent>(
                    ReadingPlanUiEvent.OnDayClick(dayNumber = 1, weekNumber = CURRENT_WEEK),
                ),
                actual = events,
            )
        }

    @Test
    fun `GIVEN a loaded plan WHEN clicking the current week THEN emits OnWeekExpandClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = readingPlanUiState())

        // When
        onNodeWithText(getString(Res.string.week_number, CURRENT_WEEK)).performClick()

        // Then
        assertEquals(
            expected = listOf<ReadingPlanUiEvent>(ReadingPlanUiEvent.OnWeekExpandClick(CURRENT_WEEK)),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a loaded plan WHEN clicking more options THEN emits OnOverflowClick`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = readingPlanUiState())

        // When
        onNodeWithContentDescription(getString(Res.string.more_options)).performClick()

        // Then
        assertEquals(expected = listOf<ReadingPlanUiEvent>(ReadingPlanUiEvent.OnOverflowClick), actual = events)
    }

    @Test
    fun `GIVEN a plan still loading WHEN rendered THEN the hero is not shown yet`() = runComposeUiTest {
        // Given
        prepareScenario(uiState = loadingUiState)

        // When
        waitForIdle()

        // Then
        onNodeWithText(getString(Res.string.hero_primary_read_now)).assertDoesNotExist()
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    private fun ComposeUiTest.prepareScenario(uiState: ReadingPlanUiState) {
        events = mutableListOf()
        setUiTestContent {
            SharedTransitionLayout {
                AnimatedContent(targetState = Unit) {
                    ReadingPlanScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                        uiState = uiState,
                        onEvent = { event -> events += event },
                        lazyListState = rememberLazyListState(),
                    )
                }
            }
        }
    }

    private companion object {
        const val CURRENT_WEEK = 16
        const val TODAY_DAY_NUMBER = 4
        const val TOTAL_DAYS = 364
        const val FIRST_DAY_GLOBAL_INDEX = (CURRENT_WEEK - 1) * 7 + 1
        const val TODAY_GLOBAL_INDEX = (CURRENT_WEEK - 1) * 7 + TODAY_DAY_NUMBER
    }
}
