package com.quare.bibleplanner.feature.day.fixture

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.presentation.DayScreen
import com.quare.bibleplanner.feature.daystudy.presentation.component.AiStudyEntryCard
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme

internal const val DAY_SCREENSHOT = "03_day"

private const val FREE_LIMIT = 3

/**
 * The day screen as the store screenshots show it, on every platform that renders them: the
 * Robolectric generators for Play and the iOS simulator captures for the App Store.
 *
 * [statusBarHeight] is the room the frame's status bar takes at the top. The Play frames reserve
 * it themselves, so they pass zero; an iOS capture fills the whole screen and passes its slot's.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DayScreenshotContent(
    locale: String,
    platform: Platform,
    statusBarHeight: Dp,
) {
    CompositionLocalProvider(LocalTheme provides Theme.DARK) {
        AppTheme {
            // Paints the room left for the status bar in the screen's own background, so an iOS
            // capture does not show a band of the frame's bezel above the top bar.
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                SharedTransitionLayout(modifier = Modifier.padding(top = statusBarHeight)) {
                    AnimatedContent(targetState = Unit) {
                        DayScreen(
                            platform = platform,
                            uiState = dayUiState(locale),
                            snackbarHostState = SnackbarHostState(),
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            isLandscape = false,
                            onEvent = {},
                            // The real card, with a state the ViewModel would otherwise have
                            // produced: the section it replaces resolves a Koin ViewModel that
                            // pulls in Supabase and Room. A null mode drops the status badge, so
                            // the listing shot advertises the feature rather than a quota.
                            dayStudySection = { _, _, sectionModifier ->
                                AiStudyEntryCard(
                                    card = DayStudyCardUiModel(
                                        mode = null,
                                        quota = Loadable.Loaded(
                                            DayStudyCardQuotaUiModel(
                                                remainingFree = FREE_LIMIT,
                                                freeLimit = FREE_LIMIT,
                                            ),
                                        ),
                                        isPro = true,
                                    ),
                                    generation = null,
                                    isOpening = false,
                                    onClick = {},
                                    modifier = sectionModifier,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
