package com.quare.bibleplanner.feature.daystudy.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_tab_context
import bibleplanner.feature.day_study.generated.resources.ai_tab_questions
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.daystudy.fixture.DAY_STUDY_CONTEXT_SCREENSHOT
import com.quare.bibleplanner.feature.daystudy.fixture.DAY_STUDY_QUESTIONS_SCREENSHOT
import com.quare.bibleplanner.feature.daystudy.fixture.DAY_STUDY_SCREENSHOT
import com.quare.bibleplanner.feature.daystudy.fixture.DayStudyScreenshotContent
import com.quare.bibleplanner.feature.daystudy.fixture.firstQuestion
import com.quare.bibleplanner.ui.testing.screenshots.AppleScreenshotSlot
import com.quare.bibleplanner.ui.testing.screenshots.captureAppleScreenshots
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

/** Renders the study on the iOS simulator for the App Store shots DayStudyScreenshots frames. */
@OptIn(ExperimentalTestApi::class)
internal class DayStudyAppleScreenshotCaptures {
    @Test
    fun dayStudy() = captureAppleScreenshots(fileName = DAY_STUDY_SCREENSHOT) { slot, locale ->
        ScreenshotContent(
            slot = slot,
            locale = locale,
        )
    }

    @Test
    fun dayStudyContext() = captureAppleScreenshots(
        fileName = DAY_STUDY_CONTEXT_SCREENSHOT,
        // The tab label is resolved rather than hard-coded so the click keeps working in every
        // locale, and keeps working if the wording changes.
        beforeCapture = {
            onNodeWithText(getString(Res.string.ai_tab_context)).performClick()
        },
    ) { slot, locale ->
        ScreenshotContent(
            slot = slot,
            locale = locale,
        )
    }

    @Test
    fun dayStudyQuestions() = captureAppleScreenshots(
        fileName = DAY_STUDY_QUESTIONS_SCREENSHOT,
        // Opening the first question shows that the tab answers them, not just lists them.
        beforeCapture = { locale ->
            onNodeWithText(getString(Res.string.ai_tab_questions)).performClick()
            onNodeWithText(firstQuestion(locale)).performClick()
        },
    ) { slot, locale ->
        ScreenshotContent(
            slot = slot,
            locale = locale,
        )
    }

    @Composable
    private fun ScreenshotContent(
        slot: AppleScreenshotSlot,
        locale: String,
    ) {
        DayStudyScreenshotContent(
            locale = locale,
            platform = Platform.Ios,
            isWide = slot == AppleScreenshotSlot.IPAD_13,
            statusBarHeight = slot.statusBarHeight,
        )
    }
}
