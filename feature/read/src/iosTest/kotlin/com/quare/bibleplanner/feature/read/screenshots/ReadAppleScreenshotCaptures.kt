package com.quare.bibleplanner.feature.read.screenshots

import androidx.compose.ui.test.ExperimentalTestApi
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.fixture.READ_SCREENSHOT
import com.quare.bibleplanner.feature.read.fixture.ReadScreenshotContent
import com.quare.bibleplanner.ui.testing.screenshots.captureAppleScreenshots
import kotlin.test.Test

/** Renders the reader on the iOS simulator for the App Store shots ReadScreenshots frames. */
@OptIn(ExperimentalTestApi::class)
internal class ReadAppleScreenshotCaptures {
    @Test
    fun read() = captureAppleScreenshots(fileName = READ_SCREENSHOT) { slot, locale ->
        ReadScreenshotContent(
            platform = Platform.Ios,
            locale = locale,
            areVersesHighlighted = false,
            statusBarHeight = slot.statusBarHeight,
        )
    }
}
