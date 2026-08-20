package com.quare.bibleplanner.feature.verse.selectionmenu.presentation

import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiAction
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val testChapter = ChapterRef(
    bibleVersionId = "ACF",
    bookId = BookId.GEN,
    chapterNumber = 3,
)

internal class VerseSelectionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)
    private lateinit var viewModel: VerseSelectionViewModel
    private lateinit var actions: List<VerseSelectionUiAction>
    private lateinit var selection: MutableStateFlow<VerseSelection?>
    private lateinit var appliedHighlights: MutableList<Pair<List<VerseRef>, HighlightColor>>
    private lateinit var clearedCount: MutableList<Unit>
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
    fun `describes the selection the reader made`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(1, 2),
            actual = viewModel.uiState.value?.verseNumbers,
        )
    }

    @Test
    fun `follows the reader as the selection grows`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        selection.value = verseSelection(listOf(1, 2, 3))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(1, 2, 3),
            actual = viewModel.uiState.value?.verseNumbers,
        )
    }

    @Test
    fun `has nothing to show once the reader empties the selection`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        selection.value = null
        runCurrent()

        // Then
        assertNull(viewModel.uiState.value)
    }

    @Test
    fun `applies the tapped color to the whole selection`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnHighlightColorClick(yellow))
        runCurrent()

        // Then
        val (refs, color) = appliedHighlights.single()
        assertEquals(
            expected = listOf(1, 2),
            actual = refs.map { it.verseNumber },
        )
        assertEquals(
            expected = yellow,
            actual = color,
        )
        assertTrue(trackedEvents.contains("verse_highlight_applied"))
    }

    @Test
    fun `closing clears the selection and pops itself`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnClearSelectionClick)
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = clearedCount.size,
        )
        assertEquals(
            expected = VerseSelectionUiAction.NavigateBack,
            actual = actions.single(),
        )
    }

    @Test
    fun `does not navigate when the reader empties the selection`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        selection.value = null
        runCurrent()

        // Then
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `opens the note editor for the selected passage`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnNoteClick)
        runCurrent()

        // Then
        assertEquals(
            expected = VerseSelectionUiAction.NavigateToRoute(
                VerseNoteNavRoute(
                    bibleVersionId = testChapter.bibleVersionId,
                    bookId = testChapter.bookId.name,
                    chapterNumber = testChapter.chapterNumber,
                    verseNumbers = listOf(1, 2),
                    noteId = null,
                ),
            ),
            actual = actions.single(),
        )
    }

    @Test
    fun `opens sharing for the selected passage`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(VerseSelectionUiEvent.OnShareClick)
        runCurrent()

        // Then
        assertEquals(
            expected = VerseSelectionUiAction.NavigateToRoute(
                ShareVerseNavRoute(
                    bookId = BookId.GEN.name,
                    chapterNumber = 3,
                    verseNumbers = listOf(1, 2),
                ),
            ),
            actual = actions.single(),
        )
    }

    private fun verseSelection(verseNumbers: List<Int>): VerseSelection = VerseSelection(
        chapter = testChapter,
        verseNumbers = verseNumbers,
    )

    private fun TestScope.prepareScenario() {
        appliedHighlights = mutableListOf()
        clearedCount = mutableListOf()
        trackedEvents = mutableListOf()
        selection = MutableStateFlow(verseSelection(listOf(1, 2)))
        viewModel = VerseSelectionViewModel(
            observeVerseSelection = { selection },
            clearVerseSelection = { clearedCount += Unit },
            observeChapterAnnotations = { _ ->
                flowOf(
                    ChapterAnnotations(
                        highlightColorByVerse = emptyMap(),
                        savedVerseNumbers = emptySet(),
                        noteIdByVerse = emptyMap(),
                    ),
                )
            },
            observeHighlightPalette = { flowOf(emptyList()) },
            applyHighlightColor = { refs, color ->
                appliedHighlights += refs to color
                true
            },
            addCustomHighlightColor = { error("unused") },
            toggleSavedVerses = { error("unused") },
            getVersesShareContent = { _, _, _ ->
                VersesShareContentModel(
                    text = "Verse text",
                    reference = "Gênesis 3:1-2",
                    versionName = "ARC",
                )
            },
            trackEvent = { name, _ -> trackedEvents += name },
        )
        actions = mutableListOf<VerseSelectionUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
