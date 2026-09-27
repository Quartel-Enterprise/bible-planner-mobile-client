package com.quare.bibleplanner.feature.chat.screenshots

import androidx.compose.ui.test.ExperimentalTestApi
import com.quare.bibleplanner.feature.chat.fixture.CHAT_SCREENSHOT
import com.quare.bibleplanner.feature.chat.fixture.ChatScreenshotContent
import com.quare.bibleplanner.ui.testing.screenshots.captureAppleScreenshots
import kotlin.test.Test

/** Renders the chat on the iOS simulator for the App Store shots ChatScreenshots frames. */
@OptIn(ExperimentalTestApi::class)
internal class ChatAppleScreenshotCaptures {
    @Test
    fun chat() = captureAppleScreenshots(fileName = CHAT_SCREENSHOT) { slot, locale ->
        ChatScreenshotContent(
            locale = locale,
            statusBarHeight = slot.statusBarHeight,
        )
    }
}
