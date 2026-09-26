package com.quare.bibleplanner.e2e

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayNotesUseCase
import com.quare.bibleplanner.e2e.harness.E2eApp
import com.quare.bibleplanner.e2e.harness.E2eWindow
import com.quare.bibleplanner.e2e.harness.awaitNode
import com.quare.bibleplanner.e2e.harness.awaitText
import com.quare.bibleplanner.e2e.harness.click
import com.quare.bibleplanner.e2e.harness.clickDescription
import com.quare.bibleplanner.e2e.harness.clickText
import org.koin.core.Koin
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class VerseNoteFlowUiTest {
    private val app = E2eApp()

    @AfterTest
    fun tearDown() {
        app.stop()
    }

    @Test
    fun `GIVEN a chapter WHEN saving a verse note and reopening the chapter THEN shows the note again`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                window = E2eWindow.PORTRAIT,
                arrange = {},
            )

            // When
            saveANoteAndReopenTheChapter()

            // Then
            awaitNode(hasSetTextAction() and hasText(NOTE))
        }

    @Test
    fun `GIVEN a wide window WHEN saving a verse note and reopening the chapter THEN shows the note again`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                window = E2eWindow.WIDE,
                arrange = {},
            )

            // When
            saveANoteAndReopenTheChapter()

            // Then
            awaitNode(hasSetTextAction() and hasText(NOTE))
        }

    @Test
    fun `GIVEN a free user with notes on as many days as allowed WHEN writing on another day THEN offers Pro`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                window = E2eWindow.PORTRAIT,
                arrange = { writeDayNotesUpToTheFreeLimit() },
            )

            // When
            clickText("Start reading")
            click(hasSetTextAction())

            // Then
            awaitText("Upgrade to Pro")
        }

    private fun ComposeUiTest.saveANoteAndReopenTheChapter() {
        clickText("Start reading")
        openTheFirstChapter()
        clickText(VERSE)
        clickText("Note")
        awaitNode(hasSetTextAction()).performTextInput(NOTE)
        click(hasText("Save") and hasAnyAncestor(isDialog()))
        clickDescription("Close")
        clickDescription("Back")
        openTheFirstChapter()
        clickText(VERSE)
        clickText("Note")
    }

    private fun ComposeUiTest.openTheFirstChapter() {
        click(hasText("Genesis") and hasText("1") and hasClickAction())
        awaitText(VERSE)
    }

    private suspend fun Koin.writeDayNotesUpToTheFreeLimit() {
        val updateDayNotes = get<UpdateDayNotesUseCase>()
        (2..FREE_NOTES_DAYS + 1).forEach { dayNumber ->
            updateDayNotes(
                weekNumber = 1,
                dayNumber = dayNumber,
                readingPlanType = ReadingPlanType.CHRONOLOGICAL,
                notes = NOTE,
            )
        }
    }

    private suspend fun ComposeUiTest.prepareScenario(
        window: E2eWindow,
        arrange: suspend Koin.() -> Unit,
    ) {
        with(app) {
            launch(
                window = window,
                arrange = arrange,
            )
        }
    }

    private companion object {
        const val VERSE = "WEB Gn 1:3"
        const val NOTE = "Light on the first day"
        const val FREE_NOTES_DAYS = 3
    }
}
