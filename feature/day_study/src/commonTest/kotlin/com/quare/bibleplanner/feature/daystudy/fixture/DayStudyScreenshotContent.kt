package com.quare.bibleplanner.feature.daystudy.fixture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.daystudy.presentation.DayStudyScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme

internal const val DAY_STUDY_SCREENSHOT = "04_day_study"

/** App Store only; see DayStudyScreenshots.dayStudyContext. */
internal const val DAY_STUDY_CONTEXT_SCREENSHOT = "07_day_study_context"

internal const val DAY_STUDY_QUESTIONS_SCREENSHOT = "08_day_study_questions"

/**
 * The study screen as the store screenshots show it, on every platform that renders them: the
 * Robolectric generators for Play and the iOS simulator captures for the App Store.
 *
 * [statusBarHeight] is the room the frame's status bar takes at the top. The Play frames reserve
 * it themselves, so they pass zero; an iOS capture fills the whole screen and passes its slot's.
 */
@Composable
internal fun DayStudyScreenshotContent(
    locale: String,
    platform: Platform,
    isWide: Boolean,
    statusBarHeight: Dp,
) {
    CompositionLocalProvider(LocalTheme provides Theme.DARK) {
        AppTheme {
            // The wide layout is a bare pane with no Scaffold, so without a Surface the frame's
            // bezel shows through behind the text.
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(modifier = Modifier.padding(top = statusBarHeight)) {
                    DayStudyScreen(
                        uiState = dayStudyUiState(
                            locale = locale,
                            platform = platform,
                        ),
                        isWide = isWide,
                        snackbarHostState = SnackbarHostState(),
                        onCardClick = {},
                        onRetryClick = {},
                        onAskAiClick = {},
                        onNavigateBack = {},
                    )
                }
            }
        }
    }
}
