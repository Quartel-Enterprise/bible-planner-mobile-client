package com.quare.bibleplanner.feature.readingplan.screenshots

import androidx.compose.ui.test.ExperimentalTestApi
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.feature.readingplan.fixture.READING_PLAN_LIGHT_SCREENSHOT
import com.quare.bibleplanner.feature.readingplan.fixture.READING_PLAN_SCREENSHOT
import com.quare.bibleplanner.feature.readingplan.fixture.ReadingPlanScreenshotContent
import com.quare.bibleplanner.ui.testing.screenshots.captureAppleScreenshots
import kotlin.test.Test

/** Renders the plan on the iOS simulator for the App Store shots ReadingPlanScreenshots frames. */
@OptIn(ExperimentalTestApi::class)
internal class ReadingPlanAppleScreenshotCaptures {
    @Test
    fun readingPlan() = captureAppleScreenshots(fileName = READING_PLAN_SCREENSHOT) { slot, _ ->
        ReadingPlanScreenshotContent(
            theme = Theme.DARK,
            statusBarHeight = slot.statusBarHeight,
        )
    }

    @Test
    fun readingPlanLight() = captureAppleScreenshots(fileName = READING_PLAN_LIGHT_SCREENSHOT) { slot, _ ->
        ReadingPlanScreenshotContent(
            theme = Theme.LIGHT,
            statusBarHeight = slot.statusBarHeight,
        )
    }
}
