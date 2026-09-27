package com.quare.bibleplanner.feature.readingplan.fixture

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.feature.readingplan.presentation.content.ReadingPlanScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme

/** The listing's dark plan shot, first on the shelf. */
internal const val READING_PLAN_SCREENSHOT = "01_reading_plan"

// Sixth, not second: "light or dark" is a weak argument to spend a first-frame slot on, and
// standing next to the dark plan it showed the same screen twice before the shopper saw any Bible
// text. The reader took the slot; see ReadScreenshots.
internal const val READING_PLAN_LIGHT_SCREENSHOT = "06_reading_plan_light"

/**
 * The plan screen as the store screenshots show it, on every platform that renders them: the
 * Robolectric generators for Play and the iOS simulator captures for the App Store.
 *
 * [statusBarHeight] is the room the frame's status bar takes at the top. The Play frames reserve
 * it themselves, so they pass zero; an iOS capture fills the whole screen and passes its slot's.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ReadingPlanScreenshotContent(
    theme: Theme,
    statusBarHeight: Dp,
) {
    CompositionLocalProvider(LocalTheme provides theme) {
        AppTheme {
            // The screen paints no background of its own — in the app that comes from the
            // Scaffold in MainRoot — so without a Surface the frame's bezel shows through behind
            // the cards.
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                SharedTransitionLayout(modifier = Modifier.padding(top = statusBarHeight)) {
                    AnimatedContent(targetState = Unit) {
                        ReadingPlanScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            uiState = readingPlanUiState(),
                            onEvent = {},
                            lazyListState = rememberLazyListState(),
                        )
                    }
                }
            }
        }
    }
}
