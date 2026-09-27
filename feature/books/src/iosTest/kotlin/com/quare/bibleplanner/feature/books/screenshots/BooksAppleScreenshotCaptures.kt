package com.quare.bibleplanner.feature.books.screenshots

import androidx.compose.ui.test.ExperimentalTestApi
import com.quare.bibleplanner.feature.books.fixture.BOOKS_SCREENSHOT
import com.quare.bibleplanner.feature.books.fixture.BooksScreenshotContent
import com.quare.bibleplanner.ui.testing.screenshots.captureAppleScreenshots
import kotlin.test.Test

/** Renders the books screen on the iOS simulator for the App Store shots BooksScreenshots frames. */
@OptIn(ExperimentalTestApi::class)
internal class BooksAppleScreenshotCaptures {
    @Test
    fun books() = captureAppleScreenshots(fileName = BOOKS_SCREENSHOT) { slot, _ ->
        BooksScreenshotContent(statusBarHeight = slot.statusBarHeight)
    }
}
