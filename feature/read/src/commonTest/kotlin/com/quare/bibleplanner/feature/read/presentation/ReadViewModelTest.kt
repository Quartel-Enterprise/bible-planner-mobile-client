package com.quare.bibleplanner.feature.read.presentation

import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.mark_as_read
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatus
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetCompletedDayForChapter
import com.quare.bibleplanner.core.plan.domain.usecase.ObserveDayCompletionCandidates
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
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
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val dayReadingCompleteAction = ReadUiAction.NavigateToRoute(
    route = DayReadingCompleteNavRoute(
        dayNumber = 2,
        weekNumber = 1,
        readingPlanType = "CHRONOLOGICAL",
    ),
    replace = false,
)
private val completedDay = PlanDayLocationModel(
    weekNumber = 1,
    dayNumber = 2,
    readingPlanType = ReadingPlanType.CHRONOLOGICAL,
)
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
    private lateinit var prefetchedDays: MutableList<PlanDayLocationModel>

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
                toggleWholeChapterReadStatus = { _, _ -> true },
                getCompletedDayForChapter = { _, _ -> completedDay },
            )

            // When
            viewModel.onEvent(ReadUiEvent.ToggleReadStatus(BookId.GEN, 3))
            runCurrent()

            // Then
            assertEquals(
                expected = dayReadingCompleteAction,
                actual = actions.last(),
            )
        }

    @Test
    fun `opens the day-complete sheet on the tap itself when the chapter is a known candidate`() =
        runTest(testDispatcher) {
            // Given
            val writeGate = CompletableDeferred<Unit>()
            prepareScenario(
                toggleWholeChapterReadStatus = { _, _ ->
                    writeGate.await()
                    true
                },
                getCompletedDayForChapter = { _, _ -> error("unused") },
                dayCompletionCandidates = mapOf(
                    ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 3) to completedDay,
                ),
            )

            // When
            viewModel.onEvent(ReadUiEvent.ToggleReadStatus(BookId.GEN, 3))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(dayReadingCompleteAction),
                actual = actions,
            )

            // When
            writeGate.complete(Unit)
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(dayReadingCompleteAction),
                actual = actions,
            )
        }

    @Test
    fun `prefetches the study quota of a day the reader is about to finish`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            dayCompletionCandidates = mapOf(
                ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 3) to completedDay,
            ),
        )

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(completedDay),
            actual = prefetchedDays,
        )
    }

    @Test
    fun `looks the day up by the chapter that was marked read`() = runTest(testDispatcher) {
        // Given
        var lookedUpChapter: Pair<BookId, Int>? = null
        prepareScenario(
            toggleWholeChapterReadStatus = { _, _ -> true },
            getCompletedDayForChapter = { bookId, chapterNumber ->
                lookedUpChapter = bookId to chapterNumber
                null
            },
        )

        // When
        viewModel.onEvent(ReadUiEvent.ToggleReadStatus(BookId.EXO, 7))
        runCurrent()

        // Then
        assertEquals(
            expected = BookId.EXO to 7,
            actual = lookedUpChapter,
        )
    }

    @Test
    fun `does not open the sheet while the day still has unread chapters`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            toggleWholeChapterReadStatus = { _, _ -> true },
            getCompletedDayForChapter = { _, _ -> null },
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
            toggleWholeChapterReadStatus = { _, _ -> false },
            getCompletedDayForChapter = { _, _ -> error("unused") },
        )

        // When
        viewModel.onEvent(ReadUiEvent.ToggleReadStatus(BookId.GEN, 3))
        runCurrent()

        // Then
        assertTrue(actions.isEmpty())
    }

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

    @Test
    fun `shows the next chapter placeholder while vertical reading looks the next chapter up`() =
        runTest(testDispatcher) {
            // Given
            val nextChapter = CompletableDeferred<ReadNavigationSuggestionModel?>()
            prepareScenario(
                isVerticalReadingEnabled = true,
                getNextChapter = { _, _, _ -> nextChapter.await() },
            )

            // When
            viewModel.onEvent(ReadUiEvent.OnReachedEnd)
            runCurrent()

            // Then
            assertTrue(viewModel.uiState.value.isLoadingNextChapter)
        }

    @Test
    fun `drops the placeholder once the next chapter is appended`() = runTest(testDispatcher) {
        // Given
        val nextChapter = CompletableDeferred<ReadNavigationSuggestionModel?>()
        prepareScenario(
            isVerticalReadingEnabled = true,
            getNextChapter = { _, _, _ -> nextChapter.await() },
        )
        viewModel.onEvent(ReadUiEvent.OnReachedEnd)
        runCurrent()

        // When
        nextChapter.complete(
            ReadNavigationSuggestionModel(
                bookId = BookId.GEN,
                chapterNumber = 4,
            ),
        )
        runCurrent()

        // Then
        assertFalse(viewModel.uiState.value.isLoadingNextChapter)
    }

    @Test
    fun `keeps the placeholder away while vertical reading is off`() = runTest(testDispatcher) {
        // Given
        prepareScenario(getNextChapter = { _, _, _ -> error("unused") })

        // When
        viewModel.onEvent(ReadUiEvent.OnReachedEnd)
        runCurrent()

        // Then
        assertFalse(viewModel.uiState.value.isLoadingNextChapter)
    }

    @Test
    fun `keeps the placeholder away at the end of the reading order`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isVerticalReadingEnabled = true)

        // When
        viewModel.onEvent(ReadUiEvent.OnReachedEnd)
        runCurrent()

        // Then
        assertFalse(viewModel.uiState.value.isLoadingNextChapter)
    }

    private fun TestScope.prepareScenario(
        toggleWholeChapterReadStatus: ToggleWholeChapterReadStatus = ToggleWholeChapterReadStatus {
            _,
            _,
            ->
            error("unused")
        },
        getCompletedDayForChapter: GetCompletedDayForChapter = GetCompletedDayForChapter { _, _ ->
            error("unused")
        },
        dayCompletionCandidates: Map<ChapterLocationModel, PlanDayLocationModel> = emptyMap(),
        isVerticalReadingEnabled: Boolean = false,
        getNextChapter: GetNextChapter = GetNextChapter { _, _, _ -> null },
    ) {
        prefetchedDays = mutableListOf()
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
                    versionAbbreviation = Loadable.Loaded("ARC"),
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
                isVerticalReadingEnabled = isVerticalReadingEnabled,
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
            toggleWholeChapterReadStatus = toggleWholeChapterReadStatus,
            isWholeChapterRead = { _, _ -> error("unused") },
            getCompletedDayForChapter = getCompletedDayForChapter,
            observeDayCompletionCandidates = ObserveDayCompletionCandidates { flowOf(dayCompletionCandidates) },
            prefetchDayStudyQuota = { day -> prefetchedDays += day },
            requestLoginNudgeIfNeeded = { },
            downloaderFacade = ThrowingBibleVersionDownloaderFacade,
            getSelectedVersionIdFlow = { error("unused") },
            requestDownloadNotificationPermission = { error("unused") },
            observeReaderSettings = { settings },
            setReaderFocusAid = { error("unused") },
            getNextChapter = getNextChapter,
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
