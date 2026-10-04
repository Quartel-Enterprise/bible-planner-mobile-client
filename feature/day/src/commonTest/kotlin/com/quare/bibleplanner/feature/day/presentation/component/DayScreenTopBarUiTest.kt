package com.quare.bibleplanner.feature.day.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.day_title_part
import bibleplanner.feature.day.generated.resources.week_title_part
import bibleplanner.ui.component.generated.resources.back
import com.quare.bibleplanner.core.plan.domain.getGlobalDayIndex
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.SharedTransitionAnimationUtils
import com.quare.bibleplanner.feature.day.fixture.dayUiState
import com.quare.bibleplanner.ui.testing.AnimationsDisabled
import com.quare.bibleplanner.ui.testing.SharedTransitionTestContent
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertTrue
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
internal class DayScreenTopBarUiTest {
    private val uiState = dayUiState(FIXTURE_LOCALE)

    private var isDayShown by mutableStateOf(false)

    @Test
    fun `GIVEN animations disabled WHEN opening a day THEN shows its week and day in the top bar`() =
        runComposeUiTest(effectContext = AnimationsDisabled) {
            // Given
            val titleParts = listOf(
                getString(Res.string.week_title_part, uiState.weekNumber),
                WEEK_SEPARATOR,
                getString(
                    Res.string.day_title_part,
                    getGlobalDayIndex(
                        weekNumber = uiState.weekNumber,
                        dayNumber = uiState.day.number,
                    ),
                ),
            )
            prepareScenario(titleParts = titleParts)

            // When
            isDayShown = true
            waitForIdle()

            // Then
            val backBounds = onNodeWithContentDescription(getString(ComponentRes.string.back))
                .getUnclippedBoundsInRoot()
            titleParts.forEach { titlePart ->
                val titlePartNode = onNodeWithText(titlePart)
                titlePartNode.assertIsDisplayed()
                val titlePartBounds = titlePartNode.getUnclippedBoundsInRoot()
                assertTrue(
                    actual = titlePartBounds.top < backBounds.bottom && titlePartBounds.bottom > backBounds.top,
                    message = "Title part at $titlePartBounds should sit in the back button row at $backBounds",
                )
            }
        }

    private fun ComposeUiTest.prepareScenario(titleParts: List<String>) {
        val titlePartKeys = listOf(
            SharedTransitionAnimationUtils.buildWeekNumberId(weekNumber = uiState.weekNumber),
            SharedTransitionAnimationUtils.buildWeekSeparatorId(uiState.weekNumber),
            SharedTransitionAnimationUtils.buildDayNumberId(
                weekNumber = uiState.weekNumber,
                dayNumebr = uiState.day.number,
            ),
        )
        setUiTestContent {
            SharedTransitionTestContent(
                isTargetShown = isDayShown,
                source = { animatedContentScope ->
                    Column {
                        titleParts.zip(titlePartKeys).forEach { (titlePart, key) ->
                            Text(
                                text = titlePart,
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(key = key),
                                    animatedVisibilityScope = animatedContentScope,
                                ),
                            )
                        }
                    }
                },
                target = { animatedContentScope ->
                    DayScreenTopBarComponent(
                        platform = Platform.Android,
                        uiState = uiState,
                        sharedTransitionScope = this,
                        animatedContentScope = animatedContentScope,
                        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                        onEvent = {},
                    )
                },
            )
        }
    }

    private companion object {
        const val FIXTURE_LOCALE = "en-US"
        const val WEEK_SEPARATOR = " — "
    }
}
