package com.quare.bibleplanner.feature.verse.addnote.presentation

import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val testChapter = ChapterRef(
    bibleVersionId = "ACF",
    bookId = BookId.GEN,
    chapterNumber = 3,
)

internal class VerseNoteViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val verseNumbers = listOf(1, 2)
    private lateinit var viewModel: VerseNoteViewModel
    private val navigator = Navigator()
    private lateinit var commands: List<NavigationCommand>
    private lateinit var savedNotes: MutableList<Pair<String?, String>>
    private lateinit var trackedEvents: MutableList<String>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opens empty for a passage without a note`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = "",
            actual = viewModel.uiState.value.text,
        )
        assertFalse(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun `loads the text of an existing note`() = runTest(testDispatcher) {
        // Given
        prepareScenario(existingNote = existingNote())

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = "Texto original",
            actual = viewModel.uiState.value.text,
        )
        assertTrue(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun `shows the passage text as the quote`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = "1 Primeiro 2 Segundo",
            actual = viewModel.uiState.value.quote,
        )
    }

    @Test
    fun `saving writes the note and leaves`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(VerseNoteUiEvent.OnTextChange("Minha reflexao"))

        // When
        viewModel.onEvent(VerseNoteUiEvent.OnSaveClick)
        runCurrent()

        // Then
        val (savedNoteId, savedText) = savedNotes.single()
        assertNull(savedNoteId)
        assertEquals(
            expected = "Minha reflexao",
            actual = savedText,
        )
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = commands,
        )
        assertTrue(trackedEvents.contains("verse_note_saved"))
    }

    @Test
    fun `dismissing leaves without writing anything`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(VerseNoteUiEvent.OnDismiss)
        runCurrent()

        // Then
        assertTrue(savedNotes.isEmpty())
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = commands,
        )
    }

    private fun existingNote(): VerseNote = VerseNote(
        id = "note-1",
        chapter = testChapter,
        verseNumbers = verseNumbers,
        text = "Texto original",
        createdAtEpochMillis = 0L,
        updatedAtEpochMillis = 0L,
    )

    private fun TestScope.prepareScenario(existingNote: VerseNote? = null) {
        savedNotes = mutableListOf()
        trackedEvents = mutableListOf()
        viewModel = VerseNoteViewModel(
            route = VerseNoteNavRoute(
                bibleVersionId = testChapter.bibleVersionId,
                bookId = testChapter.bookId.name,
                chapterNumber = testChapter.chapterNumber,
                verseNumbers = verseNumbers,
                noteId = existingNote?.id,
            ),
            getVerseNote = { existingNote },
            saveVerseNote = { noteId, _, _, text ->
                savedNotes += noteId to text
            },
            getVersesShareContent = { _, _, _ ->
                VersesShareContentModel(
                    text = "1 Primeiro 2 Segundo",
                    reference = "Gênesis 3:1-2",
                    versionAbbreviation = "ARC",
                )
            },
            navigator = navigator,
            trackEvent = { name, _ -> trackedEvents += name },
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
    }
}
