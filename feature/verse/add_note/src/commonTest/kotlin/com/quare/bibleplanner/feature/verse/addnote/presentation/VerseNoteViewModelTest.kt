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

internal class VerseNoteViewModelTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val testDispatcher = UnconfinedTestDispatcher()
    private val verseNumbers = listOf(1, 2)
    private lateinit var viewModel: VerseNoteViewModel
    private val navigator = Navigator()
    private lateinit var commands: List<NavigationCommand>
    private lateinit var savedNotes: MutableList<Pair<String?, String>>
    private lateinit var deletedNoteIds: MutableList<String>
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
    fun `GIVEN a passage without a note WHEN opening the editor THEN starts empty`() = runTest(testDispatcher) {
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
        assertFalse(viewModel.uiState.value.isExisting)
    }

    @Test
    fun `GIVEN an existing note WHEN opening the editor THEN loads its text`() = runTest(testDispatcher) {
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
    fun `GIVEN a passage WHEN opening the editor THEN shows the passage text as the quote`() = runTest(testDispatcher) {
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
    fun `GIVEN a typed text WHEN saving THEN writes the note and leaves`() = runTest(testDispatcher) {
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
    fun `GIVEN the editor WHEN dismissing it THEN leaves without writing anything`() = runTest(testDispatcher) {
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

    @Test
    fun `GIVEN an existing note WHEN tapping delete THEN asks before deleting it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(existingNote = existingNote())

        // When
        viewModel.onEvent(VerseNoteUiEvent.OnDeleteClick)
        runCurrent()

        // Then
        assertTrue(viewModel.uiState.value.isDeleteConfirmationVisible)
        assertTrue(deletedNoteIds.isEmpty())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN the delete confirmation WHEN confirming THEN removes the note tracks it and leaves`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(existingNote = existingNote())
            viewModel.onEvent(VerseNoteUiEvent.OnDeleteClick)

            // When
            viewModel.onEvent(VerseNoteUiEvent.OnDeleteConfirm)
            runCurrent()

            // Then
            assertEquals(
                expected = listOf("note-1"),
                actual = deletedNoteIds,
            )
            assertTrue(savedNotes.isEmpty())
            assertTrue(trackedEvents.contains("verse_note_deleted"))
            assertEquals(
                expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN the delete confirmation WHEN cancelling THEN keeps the note and the editor open`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(existingNote = existingNote())
            viewModel.onEvent(VerseNoteUiEvent.OnDeleteClick)

            // When
            viewModel.onEvent(VerseNoteUiEvent.OnDeleteCancel)
            runCurrent()

            // Then
            assertFalse(viewModel.uiState.value.isDeleteConfirmationVisible)
            assertTrue(deletedNoteIds.isEmpty())
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN an existing note WHEN opening the editor THEN offers editing and deleting it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(existingNote = existingNote())

            // When
            runCurrent()

            // Then
            assertTrue(viewModel.uiState.value.isExisting)
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
        deletedNoteIds = mutableListOf()
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
            deleteVerseNote = { noteId -> deletedNoteIds += noteId },
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
