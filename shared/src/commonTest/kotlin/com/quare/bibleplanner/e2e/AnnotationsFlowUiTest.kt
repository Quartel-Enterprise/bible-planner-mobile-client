package com.quare.bibleplanner.e2e

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ApplyHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.SaveVerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleSavedVerses
import com.quare.bibleplanner.e2e.harness.E2eApp
import com.quare.bibleplanner.e2e.harness.E2eWindow
import com.quare.bibleplanner.e2e.harness.FakeBibles
import com.quare.bibleplanner.e2e.harness.awaitGone
import com.quare.bibleplanner.e2e.harness.awaitNode
import com.quare.bibleplanner.e2e.harness.awaitText
import com.quare.bibleplanner.e2e.harness.click
import com.quare.bibleplanner.e2e.harness.clickDescription
import com.quare.bibleplanner.e2e.harness.clickText
import kotlinx.coroutines.flow.first
import org.koin.core.Koin
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class AnnotationsFlowUiTest {
    private val app = E2eApp()
    private val otherVersionId = FakeBibles.englishAlternative.id

    @AfterTest
    fun tearDown() {
        app.stop()
    }

    @Test
    fun `GIVEN marked verses WHEN opening one from the annotations THEN the reader lands on that verse`() =
        runComposeUiTest {
            // Given
            prepareScenario(window = E2eWindow.PORTRAIT)

            // When
            openTheAnnotationsAndTheHighlightedVerse()

            // Then
            awaitText("Mark as Read")
            awaitText(HIGHLIGHTED_VERSE).assertIsDisplayed()
        }

    @Test
    fun `GIVEN a wide window WHEN opening a marked verse from the annotations THEN the reader lands on it`() =
        runComposeUiTest {
            // Given
            prepareScenario(window = E2eWindow.WIDE)

            // When
            openTheAnnotationsAndTheHighlightedVerse()

            // Then
            awaitText("Mark as Read")
            awaitText(HIGHLIGHTED_VERSE).assertIsDisplayed()
        }

    @Test
    fun `GIVEN marked verses WHEN searching for a reference THEN lists only the matching passage`() = runComposeUiTest {
        // Given
        prepareScenario(window = E2eWindow.PORTRAIT)
        openTheAnnotations()

        // When
        clickDescription("Search")
        awaitNode(hasSetTextAction()).performTextInput("genesis 2")

        // Then
        awaitText(SAVED_VERSE)
        awaitGone(hasText(HIGHLIGHTED_VERSE))
        awaitGone(hasText(NOTED_VERSE))
    }

    @Test
    fun `GIVEN marked verses WHEN removing the first one THEN it leaves the list and the summary`() = runComposeUiTest {
        // Given
        prepareScenario(window = E2eWindow.PORTRAIT)
        openTheAnnotations()

        // When
        onAllNodes(hasContentDescription("More options")).onFirst().performClick()
        clickText("Remove")
        click(hasText("Remove") and hasAnyAncestor(isDialog()))

        // Then
        awaitGone(hasText(NOTED_VERSE))
        awaitText(HIGHLIGHTED_VERSE)
        clickDescription("Back")
        awaitText("1 highlight · 1 saved · 0 notes")
    }

    @Test
    fun `GIVEN a verse marked only in a version not downloaded WHEN using that version THEN lists the mark`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                window = E2eWindow.PORTRAIT,
                arrange = { highlightInAnotherVersion() },
            )
            clickText("Profile")
            awaitText("1 mark in $otherVersionId")
            clickText("Annotations")
            awaitText("Nothing marked in this version")
            awaitText("1 mark in $otherVersionId")

            // When
            clickText("Use $otherVersionId")

            // Then
            awaitText("Job 38:4")
            awaitGone(hasText("Nothing marked in this version"))
        }

    private fun ComposeUiTest.openTheAnnotations() {
        clickText("Profile")
        awaitText("1 highlight · 1 saved · 1 note")
        clickText("Annotations")
        awaitText(HIGHLIGHTED_VERSE)
    }

    private fun ComposeUiTest.openTheAnnotationsAndTheHighlightedVerse() {
        openTheAnnotations()
        awaitText("“$NOTE”")
        clickText("Genesis 1:25")
    }

    private suspend fun Koin.markSomeVerses() {
        val versionId = get<GetSelectedVersionIdFlow>().invoke().first()
        val genesisOne = ChapterRef(
            bibleVersionId = versionId,
            bookId = BookId.GEN,
            chapterNumber = 1,
        )
        get<ApplyHighlightColor>().invoke(
            refs = listOf(
                VerseRef(
                    chapter = genesisOne,
                    verseNumber = 25,
                ),
            ),
            color = HighlightColor.Preset(PresetHighlightColor.YELLOW),
        )
        get<ToggleSavedVerses>().invoke(
            listOf(
                VerseRef(
                    chapter = genesisOne.copy(chapterNumber = 2),
                    verseNumber = 7,
                ),
            ),
        )
        get<SaveVerseNote>().invoke(
            noteId = null,
            chapter = genesisOne,
            verseNumbers = listOf(5),
            text = NOTE,
        )
    }

    private suspend fun Koin.highlightInAnotherVersion() {
        get<ApplyHighlightColor>().invoke(
            refs = listOf(
                VerseRef(
                    chapter = ChapterRef(
                        bibleVersionId = otherVersionId,
                        bookId = BookId.JOB,
                        chapterNumber = 38,
                    ),
                    verseNumber = 4,
                ),
            ),
            color = HighlightColor.Preset(PresetHighlightColor.YELLOW),
        )
    }

    private suspend fun ComposeUiTest.prepareScenario(
        window: E2eWindow,
        arrange: suspend Koin.() -> Unit = { markSomeVerses() },
    ) {
        with(app) {
            launch(
                window = window,
                arrange = arrange,
            )
        }
    }

    private companion object {
        const val HIGHLIGHTED_VERSE = "WEB Gn 1:25"
        const val SAVED_VERSE = "WEB Gn 2:7"
        const val NOTED_VERSE = "WEB Gn 1:5"
        const val NOTE = "Evening and morning"
    }
}
