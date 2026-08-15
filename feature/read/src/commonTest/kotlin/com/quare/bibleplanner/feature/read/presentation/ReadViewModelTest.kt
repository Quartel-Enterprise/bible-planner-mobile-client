package com.quare.bibleplanner.feature.read.presentation

import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.mark_as_read
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.fake.FakeObserveReadData
import com.quare.bibleplanner.feature.read.fake.ThrowingBibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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

internal class ReadViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)
    private lateinit var viewModel: ReadViewModel
    private lateinit var actions: List<ReadUiAction>
    private lateinit var appliedHighlights: MutableList<Pair<List<VerseRef>, HighlightColor>>
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
    fun `selects the tapped verse and offers it in the selection panel`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(verseClick(2))
        runCurrent()

        // Then
        val selection = viewModel.uiState.value.selection
        assertEquals(
            expected = listOf(2),
            actual = selection?.verseNumbers,
        )
        assertTrue(trackedEvents.contains("verse_selection_toggled"))
    }

    @Test
    fun `keeps the selection sorted when verses are tapped out of order`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(verseClick(3))
        viewModel.onEvent(verseClick(1))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(1, 3),
            actual = viewModel.uiState.value.selection
                ?.verseNumbers,
        )
    }

    @Test
    fun `tapping a selected verse again drops it from the selection`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(verseClick(1))

        // When
        viewModel.onEvent(verseClick(1))
        runCurrent()

        // Then
        assertNull(viewModel.uiState.value.selection)
    }

    @Test
    fun `applies the tapped color to the whole selection`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(verseClick(1))
        viewModel.onEvent(verseClick(2))

        // When
        viewModel.onEvent(ReadUiEvent.OnHighlightColorClick(yellow))
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
    fun `clearing the selection closes the panel`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(verseClick(1))

        // When
        viewModel.onEvent(ReadUiEvent.OnClearSelectionClick)
        runCurrent()

        // Then
        assertNull(viewModel.uiState.value.selection)
    }

    @Test
    fun `opens the appearance sheet`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReadUiEvent.OnAppearanceClick)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                ReadUiAction.NavigateToRoute(
                    route = ReaderAppearanceNavRoute,
                    replace = false,
                ),
            ),
            actual = actions,
        )
    }

    @Test
    fun `toggling the side panel flips it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReadUiEvent.OnSidePanelToggleClick)
        runCurrent()

        // Then
        assertEquals(
            expected = false,
            actual = viewModel.uiState.value.isSidePanelOpen,
        )
        assertTrue(trackedEvents.contains("read_side_panel_toggled"))
    }

    private fun verseClick(verseNumber: Int): ReadUiEvent.OnVerseClick = ReadUiEvent.OnVerseClick(
        bookId = BookId.GEN,
        chapterNumber = 3,
        verseNumber = verseNumber,
    )

    private fun TestScope.prepareScenario() {
        appliedHighlights = mutableListOf()
        trackedEvents = mutableListOf()
        val bookStringResource = Res.string.mark_as_read
        val chapter = ReadChapterUiModel(
            bookId = BookId.GEN,
            bookStringResource = bookStringResource,
            chapterNumber = 3,
            isRead = false,
            verses = (1..3).map { number ->
                VerseUiModel(
                    number = number,
                    heading = null,
                    text = "Verse $number",
                    isSelected = false,
                    highlightColor = null,
                    isSaved = false,
                    noteId = null,
                )
            },
        )
        val data = MutableStateFlow(
            ReadDataUiModel(
                header = ReadHeaderUiModel(
                    bookId = BookId.GEN,
                    bookStringResource = bookStringResource,
                    chapterNumber = 3,
                    isChapterRead = false,
                    navigationSuggestions = ReadNavigationSuggestionsModel(
                        previous = null,
                        next = null,
                    ),
                    versionAbbreviation = "ARC",
                ),
                content = ReadContentUiState.Success(chapters = listOf(chapter)),
            ),
        )
        val settings = MutableStateFlow(
            ReaderSettingsModel(
                fontSizeSp = ReaderFontSize.DEFAULT,
                font = ReaderFont.LORA,
                isRulerEnabled = false,
                isFocusedVerseEnabled = false,
                isVerticalReadingEnabled = false,
            ),
        )
        viewModel = ReadViewModel(
            route = ReadNavRoute(
                bookId = BookId.GEN.name,
                chapterNumber = 3,
                isChapterRead = false,
                isFromBookDetails = false,
            ),
            observeReadData = FakeObserveReadData(data),
            toggleWholeChapterReadStatus = { _, _ -> error("unused") },
            isWholeChapterRead = { _, _ -> error("unused") },
            requestLoginNudgeIfNeeded = { error("unused") },
            downloaderFacade = ThrowingBibleVersionDownloaderFacade,
            getSelectedVersionIdFlow = { error("unused") },
            requestDownloadNotificationPermission = { error("unused") },
            observeReaderSettings = { settings },
            setReaderFocusAid = { error("unused") },
            observeHighlightPalette = { MutableStateFlow(emptyList()) },
            applyHighlightColor = { refs, color ->
                appliedHighlights += refs to color
                true
            },
            addCustomHighlightColor = { error("unused") },
            toggleSavedVerses = { error("unused") },
            getVersesShareContent = { _, _, _ -> error("unused") },
            trackEvent = { name, _ -> trackedEvents += name },
            platform = Platform.Android,
        )
        actions = mutableListOf<ReadUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
