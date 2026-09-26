package com.quare.bibleplanner.e2e

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.quare.bibleplanner.e2e.harness.E2eApp
import com.quare.bibleplanner.e2e.harness.E2eWindow
import com.quare.bibleplanner.e2e.harness.awaitNode
import com.quare.bibleplanner.e2e.harness.awaitText
import com.quare.bibleplanner.e2e.harness.click
import com.quare.bibleplanner.e2e.harness.clickDescription
import com.quare.bibleplanner.e2e.harness.clickText
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class BookChapterFlowUiTest {
    private val app = E2eApp()

    @AfterTest
    fun tearDown() {
        app.stop()
    }

    @Test
    fun `GIVEN the books tab WHEN reading a chapter of a book THEN counts it in the book progress`() =
        runComposeUiTest {
            // Given
            prepareScenario(window = E2eWindow.PORTRAIT)

            // When
            readAChapterFromTheBooksTab()

            // Then
            awaitNode(bookProgress(readChapters = 1))
        }

    @Test
    fun `GIVEN a wide window WHEN reading a chapter from the books tab THEN counts it in the book progress`() =
        runComposeUiTest {
            // Given
            prepareScenario(window = E2eWindow.WIDE)

            // When
            readAChapterFromTheBooksTab()

            // Then
            awaitNode(bookProgress(readChapters = 1))
        }

    private fun ComposeUiTest.readAChapterFromTheBooksTab() {
        clickText("Books")
        clickText("Genesis")
        awaitNode(bookProgress(readChapters = 0))
        click(hasText("3") and hasClickAction())
        awaitText("WEB Gn 3:1")
        clickText("Mark as Read")
        clickDescription("Back")
    }

    private fun bookProgress(readChapters: Int): SemanticsMatcher =
        hasText("$readChapters") and hasAnyAncestor(hasAnyChild(hasText("Reading Progress")))

    private suspend fun ComposeUiTest.prepareScenario(window: E2eWindow) {
        with(app) {
            launch(
                window = window,
                arrange = {},
            )
        }
    }
}
