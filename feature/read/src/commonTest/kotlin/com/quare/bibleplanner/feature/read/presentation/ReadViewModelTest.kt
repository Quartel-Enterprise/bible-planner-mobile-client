package com.quare.bibleplanner.feature.read.presentation

import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.mark_as_read
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatus
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetDay
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.fake.FakeObserveReadData
import com.quare.bibleplanner.feature.read.fake.FakeVerseSelectionStore
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

private val testChapter = ChapterRef(
    bibleVersionId = "ACF",
    bookId = BookId.GEN,
    chapterNumber = 3,
)

internal class ReadViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ReadViewModel
    private lateinit var actions: List<ReadUiAction>
    private lateinit var selectionStore: FakeVerseSelectionStore
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
    fun `marks the tapped verse as selected in the chapter`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(verseClick(2))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(2),
            actual = selectedVerseNumbers(),
        )
        assertTrue(trackedEvents.contains("verse_selection_toggled"))
    }

    @Test
    fun `opens the selection panel the moment the first verse is picked`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(verseClick(2))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                ReadUiAction.NavigateToRoute(
                    route = VerseSelectionNavRoute,
                    replace = false,
                ),
            ),
            actual = actions,
        )
    }

    @Test
    fun `does not reopen the panel while the selection grows`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(verseClick(1))

        // When
        viewModel.onEvent(verseClick(2))
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = actions.size,
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
        assertNull(selectionStore.selection.value)
        assertTrue(selectedVerseNumbers().isEmpty())
    }

    @Test
    fun `closes the panel when the last selected verse is dropped`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(verseClick(1))

        // When
        viewModel.onEvent(verseClick(1))
        runCurrent()

        // Then
        assertEquals(
            expected = ReadUiAction.NavigateBack,
            actual = actions.last(),
        )
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
    fun `opens the day-complete sheet when the last unread chapter of the day is marked read`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                weekNumber = 1,
                dayNumber = 2,
                readingPlanType = ReadingPlanType.CHRONOLOGICAL,
                toggleWholeChapterReadStatus = { _, _ -> true },
                getDay = { _, _, _ -> dayModel(isRead = true) },
            )

            // When
            viewModel.onEvent(ReadUiEvent.ToggleReadStatus(BookId.GEN, 3))
            runCurrent()

            // Then
            assertEquals(
                expected = ReadUiAction.NavigateToRoute(
                    route = DayReadingCompleteNavRoute(
                        dayNumber = 2,
                        weekNumber = 1,
                        readingPlanType = "CHRONOLOGICAL",
                    ),
                    replace = false,
                ),
                actual = actions.last(),
            )
        }

    @Test
    fun `does not open the sheet while the day still has unread chapters`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            weekNumber = 1,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            toggleWholeChapterReadStatus = { _, _ -> true },
            getDay = { _, _, _ -> dayModel(isRead = false) },
        )

        // When
        viewModel.onEvent(ReadUiEvent.ToggleReadStatus(BookId.GEN, 3))
        runCurrent()

        // Then
        assertTrue(actions.none { it is ReadUiAction.NavigateToRoute && it.route is DayReadingCompleteNavRoute })
    }

    @Test
    fun `does not open the sheet when the chapter is unmarked instead of marked read`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            weekNumber = 1,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            toggleWholeChapterReadStatus = { _, _ -> false },
            getDay = { _, _, _ -> error("unused") },
        )

        // When
        viewModel.onEvent(ReadUiEvent.ToggleReadStatus(BookId.GEN, 3))
        runCurrent()

        // Then
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `does not open the sheet when the route has no reading-plan day context`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            weekNumber = null,
            dayNumber = null,
            readingPlanType = null,
            toggleWholeChapterReadStatus = { _, _ -> true },
            getDay = { _, _, _ -> error("unused") },
        )

        // When
        viewModel.onEvent(ReadUiEvent.ToggleReadStatus(BookId.GEN, 3))
        runCurrent()

        // Then
        assertTrue(actions.isEmpty())
    }

    private fun dayModel(isRead: Boolean): DayModel = DayModel(
        number = 2,
        passages = emptyList(),
        isRead = isRead,
        totalVerses = 0,
        readVerses = 0,
        readTimestamp = null,
        plannedReadDate = null,
        notes = null,
        isToday = true,
    )

    private fun selectedVerseNumbers(): List<Int> = (viewModel.uiState.value.content as ReadContentUiState.Success)
        .chapters
        .single()
        .verses
        .filter { it.isSelected }
        .map { it.number }

    private fun verseClick(verseNumber: Int): ReadUiEvent.OnVerseClick = ReadUiEvent.OnVerseClick(
        chapter = testChapter,
        verseNumber = verseNumber,
    )

    private fun TestScope.prepareScenario(
        weekNumber: Int? = null,
        dayNumber: Int? = null,
        readingPlanType: ReadingPlanType? = null,
        toggleWholeChapterReadStatus: ToggleWholeChapterReadStatus = ToggleWholeChapterReadStatus {
            _,
            _,
            ->
            error("unused")
        },
        getDay: GetDay = GetDay { _, _, _ -> error("unused") },
    ) {
        trackedEvents = mutableListOf()
        selectionStore = FakeVerseSelectionStore()
        val bookStringResource = Res.string.mark_as_read
        val chapter = ReadChapterUiModel(
            chapter = testChapter,
            bookStringResource = bookStringResource,
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
                rulerLines = ReaderRulerLines.DEFAULT,
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
                weekNumber = weekNumber,
                dayNumber = dayNumber,
                readingPlanType = readingPlanType?.name,
            ),
            observeReadData = FakeObserveReadData(data),
            toggleWholeChapterReadStatus = toggleWholeChapterReadStatus,
            isWholeChapterRead = { _, _ -> error("unused") },
            getDay = getDay,
            requestLoginNudgeIfNeeded = { },
            downloaderFacade = ThrowingBibleVersionDownloaderFacade,
            getSelectedVersionIdFlow = { error("unused") },
            requestDownloadNotificationPermission = { error("unused") },
            observeReaderSettings = { settings },
            setReaderFocusAid = { error("unused") },
            getNextChapter = { _, _, _ -> null },
            observeVerseSelection = { selectionStore.selection },
            toggleVerseSelection = { chapter, verseNumber ->
                selectionStore.toggle(
                    chapter = chapter,
                    verseNumber = verseNumber,
                )
            },
            clearVerseSelection = { selectionStore.clear() },
            trackEvent = { name, _ -> trackedEvents += name },
            platform = Platform.Android,
        )
        actions = mutableListOf<ReadUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
