package com.quare.bibleplanner.feature.day.screenshots

import androidx.compose.ui.test.ExperimentalTestApi
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.day.fixture.DAY_SCREENSHOT
import com.quare.bibleplanner.feature.day.fixture.DayScreenshotContent
import com.quare.bibleplanner.ui.testing.screenshots.captureAppleScreenshots
import kotlin.test.Test

/** Renders the day on the iOS simulator for the App Store shots DayScreenshots frames. */
@OptIn(ExperimentalTestApi::class)
internal class DayAppleScreenshotCaptures {
    @Test
    fun day() = captureAppleScreenshots(fileName = DAY_SCREENSHOT) { slot, locale ->
        DayScreenshotContent(
            locale = locale,
            platform = Platform.Ios,
            statusBarHeight = slot.statusBarHeight,
        )
    }
}
