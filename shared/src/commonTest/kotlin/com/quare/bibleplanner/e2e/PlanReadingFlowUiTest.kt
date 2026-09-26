package com.quare.bibleplanner.e2e

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isOn
import androidx.compose.ui.test.isToggleable
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
internal class PlanReadingFlowUiTest {
    private val app = E2eApp()

    @AfterTest
    fun tearDown() {
        app.stop()
    }

    @Test
    fun `GIVEN a new plan WHEN reading every chapter of the first day THEN completes it and moves the progress`() =
        runComposeUiTest {
            // Given
            prepareScenario(window = E2eWindow.PORTRAIT)

            // When
            readTheFirstDay()

            // Then
            assertTheFirstDayIsComplete()
        }

    @Test
    fun `GIVEN a wide window WHEN reading every chapter of the first day THEN completes it and moves the progress`() =
        runComposeUiTest {
            // Given
            prepareScenario(window = E2eWindow.WIDE)

            // When
            readTheFirstDay()

            // Then
            assertTheFirstDayIsComplete()
        }

    private fun ComposeUiTest.readTheFirstDay() {
        awaitText("0 of 364 days")
        clickText("Start reading")
        click(chapterRow(chapter = 1))
        awaitText("WEB Gn 1:1")
        clickText("Mark as Read")
        clickDescription("Back")
        awaitNode(chapterCheckbox(chapter = 1) and isOn())
        click(chapterCheckbox(chapter = 2))
        click(chapterCheckbox(chapter = 3))
    }

    private fun ComposeUiTest.assertTheFirstDayIsComplete() {
        awaitNode(chapterCheckbox(chapter = 2) and isOn())
        awaitNode(chapterCheckbox(chapter = 3) and isOn())
        awaitText("Completed date")
        clickDescription("Back")
        awaitText("1 of 364 days")
        awaitText("1 of 7 days")
    }

    private fun chapterRow(chapter: Int): SemanticsMatcher = hasText(BOOK) and hasText("$chapter") and hasClickAction()

    private fun chapterCheckbox(chapter: Int): SemanticsMatcher =
        isToggleable() and hasParent(hasText(BOOK) and hasText("$chapter"))

    private suspend fun ComposeUiTest.prepareScenario(window: E2eWindow) {
        with(app) {
            launch(
                window = window,
                arrange = {},
            )
        }
    }

    private companion object {
        const val BOOK = "Genesis"
    }
}
