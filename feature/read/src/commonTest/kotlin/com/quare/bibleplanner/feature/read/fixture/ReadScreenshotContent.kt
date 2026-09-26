package com.quare.bibleplanner.feature.read.fixture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.presentation.screen.ReadNarrowScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme

// Second on purpose, not in reading order: a shopper who never swipes sees the first two or three
// shots, and this is the only one that shows the Bible text itself. The listings spent years
// telling people the app did not include it — see docs/store-listing-metadata.md — so the reader
// leads instead of the light theme.
internal const val READ_SCREENSHOT = "02_read"

/**
 * The reader as the store screenshots show it, on every platform that renders them: the
 * Robolectric generators for Play and the iOS simulator captures for the App Store.
 *
 * The screen branches on [platform] — the back arrow is a chevron on Apple and a left arrow
 * elsewhere — so each side passes the platform its store belongs to.
 *
 * [statusBarHeight] is the room the frame's status bar takes at the top. The Play frames reserve
 * it themselves, so they pass zero; an iOS capture fills the whole screen and passes its slot's.
 */
@Composable
internal fun ReadScreenshotContent(
    platform: Platform,
    locale: String,
    areVersesHighlighted: Boolean,
    statusBarHeight: Dp,
) {
    CompositionLocalProvider(LocalTheme provides Theme.DARK) {
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(modifier = Modifier.padding(top = statusBarHeight)) {
                    ReadNarrowScreen(
                        platform = platform,
                        state = readUiState(
                            locale = locale,
                            areVersesHighlighted = areVersesHighlighted,
                        ),
                        onEvent = {},
                        dayCompletionBanner = NoDayCompletionBanner,
                    )
                }
            }
        }
    }
}
