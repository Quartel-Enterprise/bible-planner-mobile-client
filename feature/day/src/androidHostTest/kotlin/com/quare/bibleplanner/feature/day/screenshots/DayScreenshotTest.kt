package com.quare.bibleplanner.feature.day.screenshots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.fixture.dayUiState
import com.quare.bibleplanner.feature.day.presentation.DayScreen
import com.quare.bibleplanner.feature.day.presentation.model.DayUiState
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotTest
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotVariant
import org.junit.Test

internal class DayScreenshotTest : ScreenshotTest() {
    private val loadedUiState = dayUiState(FIXTURE_LOCALE)

    @Test
    fun loading() {
        snapshot(name = "loading") {
            DayContent(uiState = DayUiState.Loading)
        }
    }

    @Test
    fun loaded() {
        snapshot(
            name = "loaded",
            variants = ScreenshotVariant.all,
        ) {
            DayContent(uiState = loadedUiState)
        }
    }

    @Test
    fun loadedLandscape() {
        snapshot(
            name = "loaded_landscape",
            isLandscape = true,
        ) {
            DayContent(
                uiState = loadedUiState,
                isLandscape = true,
            )
        }
    }

    @Test
    fun read() {
        snapshot(name = "read") {
            DayContent(uiState = loadedUiState.copy(day = loadedUiState.day.copy(isRead = true)))
        }
    }

    @Test
    fun withoutNotes() {
        snapshot(name = "without_notes") {
            DayContent(uiState = loadedUiState.copy(day = loadedUiState.day.copy(notes = null)))
        }
    }

    @Composable
    private fun DayContent(
        uiState: DayUiState,
        isLandscape: Boolean = false,
    ) {
        SharedTransitionLayout {
            AnimatedContent(targetState = Unit) {
                DayScreen(
                    platform = Platform.Android,
                    uiState = uiState,
                    snackbarHostState = remember { SnackbarHostState() },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    isLandscape = isLandscape,
                    onEvent = {},
                    dayStudySection = { _, _, _ -> },
                )
            }
        }
    }

    private companion object {
        const val FIXTURE_LOCALE = "en-US"
    }
}
