package com.quare.bibleplanner.feature.verse.annotations.presentation.viewmodel

import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.annotation_removed
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningType
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import com.quare.bibleplanner.feature.verse.annotations.fixture.sampleNow
import com.quare.bibleplanner.feature.verse.annotations.fixture.samplePassage
import com.quare.bibleplanner.feature.verse.annotations.fixture.toEntry
import com.quare.bibleplanner.feature.verse.annotations.fixture.utcLocalDateTimeProvider
import com.quare.bibleplanner.feature.verse.annotations.fixture.yellow
import com.quare.bibleplanner.feature.verse.annotations.presentation.factory.AnnotationsContentFactory
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationDateRange
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationFilterMenu
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationItemUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationTypeFilter
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiAction
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiState
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.OtherVersionAnnotationsUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AnnotationsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val highlightedPassage = samplePassage(
        bookId = BookId.JHN,
        chapterNumber = 3,
        verseNumbers = listOf(16, 17),
        highlightColor = yellow,
    )
    private val notedPassage = samplePassage(
        bookId = BookId.GEN,
        chapterNumber = 3,
        verseNumbers = listOf(15),
        noteText = "First promise",
    )
    private lateinit var entries: MutableSharedFlow<List<AnnotationEntry>>
    private lateinit var viewModel: AnnotationsViewModel
    private lateinit var states: List<AnnotationsUiState>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var actions: List<AnnotationsUiAction>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var removedPassages: MutableList<AnnotatedPassage>
    private lateinit var otherVersionCounts: MutableStateFlow<List<VersionAnnotationCount>>
    private lateinit var selectedVersionIds: MutableList<String>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN no entries yet WHEN observing THEN starts loading and then shows the list`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val initial = states.last().content
        entries.emit(listOf(highlightedPassage.toEntry(), notedPassage.toEntry()))

        // Then
        assertEquals(
            expected = Loadable.Loading,
            actual = initial,
        )
        assertEquals(
            expected = 2,
            actual = states
                .last()
                .content
                .valueOrNull()
                ?.shownCount,
        )
    }

    @Test
    fun `GIVEN the search WHEN typing and clearing THEN filters by the query and back`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnSearchClick)
        val isSearchOpen = states.last().isSearchOpen

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnSearchQueryChange("GEN"))
        val searched = shownPassages()
        val query = states.last().searchQuery
        viewModel.onEvent(AnnotationsUiEvent.OnSearchClearClick)

        // Then
        assertEquals(
            expected = true,
            actual = isSearchOpen,
        )
        assertEquals(
            expected = listOf(notedPassage),
            actual = searched,
        )
        assertEquals(
            expected = "GEN",
            actual = query,
        )
        assertEquals(
            expected = "",
            actual = states.last().searchQuery,
        )
        assertEquals(
            expected = true,
            actual = states.last().isSearchOpen,
        )
        assertEquals(
            expected = 2,
            actual = shownPassages().size,
        )
    }

    @Test
    fun `GIVEN a search WHEN closing it THEN hides the field and drops the query`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnSearchClick)
        viewModel.onEvent(AnnotationsUiEvent.OnSearchQueryChange("GEN"))

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnSearchCloseClick)

        // Then
        assertEquals(
            expected = false,
            actual = states.last().isSearchOpen,
        )
        assertEquals(
            expected = "",
            actual = states.last().searchQuery,
        )
        assertEquals(
            expected = 2,
            actual = shownPassages().size,
        )
    }

    @Test
    fun `GIVEN a search WHEN clearing the filters THEN drops the query too`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnSearchClick)
        viewModel.onEvent(AnnotationsUiEvent.OnSearchQueryChange("nothing matches"))

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnClearFiltersClick)

        // Then
        assertEquals(
            expected = "",
            actual = states.last().searchQuery,
        )
        assertEquals(
            expected = 2,
            actual = shownPassages().size,
        )
    }

    @Test
    fun `GIVEN entries WHEN choosing a type THEN shows only that type`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnTypeFilterClick(AnnotationTypeFilter.NOTES))

        // Then
        assertEquals(
            expected = listOf(notedPassage),
            actual = shownPassages(),
        )
        assertEquals(
            expected = AnalyticsEventNames.ANNOTATIONS_TYPE_FILTER_CHANGED to
                mapOf<String, Any>(AnalyticsParams.FILTER_TYPE to "notes"),
            actual = trackedEvents.last(),
        )
    }

    @Test
    fun `GIVEN a selected type WHEN tapping it again THEN goes back to every type`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnTypeFilterClick(AnnotationTypeFilter.NOTES))

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnTypeFilterClick(AnnotationTypeFilter.NOTES))

        // Then
        assertEquals(
            expected = 2,
            actual = shownPassages().size,
        )
        assertEquals(
            expected = listOf("notes", "all"),
            actual = trackedEvents
                .filter { it.first == AnalyticsEventNames.ANNOTATIONS_TYPE_FILTER_CHANGED }
                .map { it.second[AnalyticsParams.FILTER_TYPE] },
        )
    }

    @Test
    fun `GIVEN a selected book WHEN picking it again THEN goes back to every book`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnBookSelected(BookId.GEN))

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnBookSelected(BookId.GEN))

        // Then
        assertNull(
            states
                .last()
                .content
                .valueOrNull()
                ?.selectedBookId,
        )
        assertEquals(
            expected = listOf("gen", "all"),
            actual = trackedEvents
                .filter { it.first == AnalyticsEventNames.ANNOTATIONS_BOOK_FILTER_CHANGED }
                .map { it.second[AnalyticsParams.BOOK_ID] },
        )
    }

    @Test
    fun `GIVEN a selected period WHEN picking it again THEN goes back to any date`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.TODAY))

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.TODAY))

        // Then
        assertEquals(
            expected = AnnotationPeriod.ANY,
            actual = states
                .last()
                .content
                .valueOrNull()
                ?.selectedPeriod,
        )
        assertEquals(
            expected = listOf("today", "any"),
            actual = trackedEvents
                .filter { it.first == AnalyticsEventNames.ANNOTATIONS_PERIOD_FILTER_CHANGED }
                .map { it.second[AnalyticsParams.PERIOD] },
        )
    }

    @Test
    fun `GIVEN the custom period WHEN picking a range THEN keeps what was marked in it`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.CUSTOM))
        val isPickerOpen = states.last().isCustomRangePickerOpen

        // When
        viewModel.onEvent(
            AnnotationsUiEvent.OnCustomRangeApply(
                startUtcMillis = utcMillisOf(day = 26),
                endUtcMillis = utcMillisOf(day = 20),
            ),
        )

        // Then
        val content = states.last().content.valueOrNull()
        assertEquals(
            expected = true,
            actual = isPickerOpen,
        )
        assertEquals(
            expected = false,
            actual = states.last().isCustomRangePickerOpen,
        )
        assertEquals(
            expected = AnnotationDateRange(
                start = LocalDate(
                    year = 2026,
                    month = 9,
                    day = 20,
                ),
                end = LocalDate(
                    year = 2026,
                    month = 9,
                    day = 26,
                ),
            ),
            actual = content?.customRange,
        )
        assertEquals(
            expected = AnnotationPeriod.CUSTOM,
            actual = content?.selectedPeriod,
        )
        assertEquals(
            expected = 2,
            actual = shownPassages().size,
        )
        assertEquals(
            expected = listOf(
                AnalyticsEventNames.ANNOTATIONS_CUSTOM_RANGE_OPENED to emptyMap<String, Any>(),
                AnalyticsEventNames.ANNOTATIONS_PERIOD_FILTER_CHANGED to
                    mapOf<String, Any>(AnalyticsParams.PERIOD to "custom"),
            ),
            actual = trackedEvents.takeLast(2),
        )
    }

    @Test
    fun `GIVEN a single day WHEN applying the range THEN uses it as both ends`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.CUSTOM))

        // When
        viewModel.onEvent(
            AnnotationsUiEvent.OnCustomRangeApply(
                startUtcMillis = utcMillisOf(day = 25),
                endUtcMillis = null,
            ),
        )

        // Then
        val range = states
            .last()
            .content
            .valueOrNull()
            ?.customRange
        assertEquals(
            expected = range?.start,
            actual = range?.end,
        )
        assertEquals(
            expected = 0,
            actual = shownPassages().size,
        )
    }

    @Test
    fun `GIVEN a custom range WHEN picking custom again THEN goes back to any date`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.CUSTOM))
        viewModel.onEvent(
            AnnotationsUiEvent.OnCustomRangeApply(
                startUtcMillis = utcMillisOf(day = 25),
                endUtcMillis = null,
            ),
        )

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.CUSTOM))

        // Then
        val content = states.last().content.valueOrNull()
        assertEquals(
            expected = AnnotationPeriod.ANY,
            actual = content?.selectedPeriod,
        )
        assertNull(content?.customRange)
        assertEquals(
            expected = false,
            actual = states.last().isCustomRangePickerOpen,
        )
    }

    @Test
    fun `GIVEN the range picker WHEN dismissing it THEN keeps the previous period`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.TODAY))
        viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.CUSTOM))

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnCustomRangeDismiss)

        // Then
        assertEquals(
            expected = false,
            actual = states.last().isCustomRangePickerOpen,
        )
        assertEquals(
            expected = AnnotationPeriod.TODAY,
            actual = states
                .last()
                .content
                .valueOrNull()
                ?.selectedPeriod,
        )
    }

    @Test
    fun `GIVEN a colour WHEN tapping it twice THEN selects and clears it and tracks both`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnColorFilterClick(yellow))
        val filtered = shownPassages()
        viewModel.onEvent(AnnotationsUiEvent.OnColorFilterClick(yellow))

        // Then
        assertEquals(
            expected = listOf(highlightedPassage),
            actual = filtered,
        )
        assertEquals(
            expected = 2,
            actual = shownPassages().size,
        )
        assertEquals(
            expected = listOf(true, false),
            actual = trackedEvents
                .filter { it.first == AnalyticsEventNames.ANNOTATIONS_COLOR_FILTER_TOGGLED }
                .map { it.second[AnalyticsParams.IS_SELECTED] },
        )
    }

    @Test
    fun `GIVEN the book menu WHEN picking a book THEN filters by it and closes the menu`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnBookFilterClick)
        val openedMenu = states.last().openFilterMenu

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnBookSelected(BookId.GEN))

        // Then
        assertEquals(
            expected = AnnotationFilterMenu.BOOK,
            actual = openedMenu,
        )
        assertNull(states.last().openFilterMenu)
        assertEquals(
            expected = listOf(notedPassage),
            actual = shownPassages(),
        )
    }

    @Test
    fun `GIVEN the period menu WHEN picking a period and then clearing THEN restores every entry`() =
        runTest(testDispatcher) {
            // Given
            prepareLoadedScenario()
            viewModel.onEvent(AnnotationsUiEvent.OnPeriodFilterClick)
            viewModel.onEvent(AnnotationsUiEvent.OnFilterMenuDismiss)
            viewModel.onEvent(AnnotationsUiEvent.OnPeriodSelected(AnnotationPeriod.TODAY))
            viewModel.onEvent(AnnotationsUiEvent.OnTypeFilterClick(AnnotationTypeFilter.SAVED))
            val filteredCount = shownPassages().size

            // When
            viewModel.onEvent(AnnotationsUiEvent.OnClearFiltersClick)

            // Then
            assertEquals(
                expected = 0,
                actual = filteredCount,
            )
            assertEquals(
                expected = 2,
                actual = shownPassages().size,
            )
            assertEquals(
                expected = AnnotationPeriod.ANY,
                actual = states
                    .last()
                    .content
                    .valueOrNull()
                    ?.selectedPeriod,
            )
        }

    @Test
    fun `GIVEN an item WHEN tapping it THEN opens its chapter with its read state`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario(isChapterRead = true)

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnItemClick(item(highlightedPassage)))

        // Then
        assertEquals(
            expected = listOf(
                NavigationCommand.Navigate(
                    ReadNavRoute(
                        bookId = "JHN",
                        chapterNumber = 3,
                        isChapterRead = true,
                        isFromBookDetails = false,
                        targetVerseNumbers = listOf(16, 17),
                    ),
                ),
            ),
            actual = commands,
        )
        assertEquals(
            expected = mapOf<String, Any>(
                AnalyticsParams.SOURCE to "row",
                AnalyticsParams.BOOK_ID to "jhn",
                AnalyticsParams.CHAPTER_NUMBER to 3,
            ),
            actual = trackedEvents.single { it.first == AnalyticsEventNames.ANNOTATION_CHAPTER_OPENED }.second,
        )
    }

    @Test
    fun `GIVEN an item menu WHEN picking its actions THEN each one closes the menu and navigates`() =
        runTest(testDispatcher) {
            // Given
            prepareLoadedScenario()
            val noted = item(notedPassage)
            val highlighted = item(highlightedPassage)

            // When
            viewModel.onEvent(AnnotationsUiEvent.OnItemMenuClick(noted))
            val openedMenuKey = states.last().openMenuItemKey
            viewModel.onEvent(AnnotationsUiEvent.OnNoteClick(noted))
            viewModel.onEvent(AnnotationsUiEvent.OnNoteClick(highlighted))
            viewModel.onEvent(AnnotationsUiEvent.OnShareClick(highlighted))
            viewModel.onEvent(AnnotationsUiEvent.OnOpenInChapterClick(noted))

            // Then
            assertEquals(
                expected = noted.key,
                actual = openedMenuKey,
            )
            assertNull(states.last().openMenuItemKey)
            assertEquals(
                expected = listOf(
                    VerseNoteNavRoute(
                        bibleVersionId = "arc",
                        bookId = "GEN",
                        chapterNumber = 3,
                        verseNumbers = listOf(15),
                        noteId = notedPassage.note?.id,
                    ),
                    VerseNoteNavRoute(
                        bibleVersionId = "arc",
                        bookId = "JHN",
                        chapterNumber = 3,
                        verseNumbers = listOf(16, 17),
                        noteId = null,
                    ),
                    ShareVerseNavRoute(
                        bookId = "JHN",
                        chapterNumber = 3,
                        verseNumbers = listOf(16, 17),
                    ),
                    ReadNavRoute(
                        bookId = "GEN",
                        chapterNumber = 3,
                        isChapterRead = false,
                        isFromBookDetails = false,
                        targetVerseNumbers = listOf(15),
                    ),
                ).map(NavigationCommand::Navigate),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN the verse notes limit is reached WHEN adding a note to an unnoted passage THEN opens the warning`() =
        runTest(testDispatcher) {
            // Given
            prepareLoadedScenario(isAddVerseNoteBlocked = true)

            // When
            viewModel.onEvent(AnnotationsUiEvent.OnNoteClick(item(highlightedPassage)))

            // Then
            assertEquals(
                expected = listOf(
                    NavigationCommand.Navigate(
                        AddNotesFreeWarningNavRoute(
                            maxFreeNotesAmount = MAX_FREE_VERSE_NOTES,
                            type = AddNotesFreeWarningType.VERSE,
                        ),
                    ),
                ),
                actual = commands,
            )
            assertEquals(
                expected = mapOf<String, Any>(
                    AnalyticsParams.MAX_FREE_NOTES to MAX_FREE_VERSE_NOTES,
                    AnalyticsParams.SOURCE to "annotations",
                ),
                actual = trackedEvents.single { it.first == AnalyticsEventNames.VERSE_NOTES_LIMIT_REACHED }.second,
            )
        }

    @Test
    fun `GIVEN the verse notes limit is reached WHEN opening an existing note THEN still opens the editor`() =
        runTest(testDispatcher) {
            // Given
            prepareLoadedScenario(isAddVerseNoteBlocked = true)

            // When
            viewModel.onEvent(AnnotationsUiEvent.OnNoteClick(item(notedPassage)))

            // Then
            assertEquals(
                expected = listOf(
                    NavigationCommand.Navigate(
                        VerseNoteNavRoute(
                            bibleVersionId = "arc",
                            bookId = "GEN",
                            chapterNumber = 3,
                            verseNumbers = listOf(15),
                            noteId = notedPassage.note?.id,
                        ),
                    ),
                ),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN an item menu WHEN dismissing it THEN closes it`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnItemMenuClick(item(notedPassage)))

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnItemMenuDismiss)

        // Then
        assertNull(states.last().openMenuItemKey)
    }

    @Test
    fun `GIVEN the removal dialog WHEN confirming THEN removes the passage and says so`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        val highlighted = item(highlightedPassage)
        viewModel.onEvent(AnnotationsUiEvent.OnRemoveClick(highlighted))
        val pending = states.last().pendingRemoval

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnRemoveConfirm(highlighted))

        // Then
        assertEquals(
            expected = highlighted,
            actual = pending,
        )
        assertNull(states.last().pendingRemoval)
        assertEquals(
            expected = listOf(highlightedPassage),
            actual = removedPassages,
        )
        assertEquals(
            expected = listOf(AnnotationsUiAction.ShowMessage(Res.string.annotation_removed)),
            actual = actions,
        )
    }

    @Test
    fun `GIVEN the removal dialog WHEN cancelling THEN keeps the passage`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()
        viewModel.onEvent(AnnotationsUiEvent.OnRemoveClick(item(notedPassage)))

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnRemoveCancel)

        // Then
        assertNull(states.last().pendingRemoval)
        assertEquals(
            expected = emptyList(),
            actual = removedPassages,
        )
    }

    @Test
    fun `GIVEN the screen WHEN tapping back THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareLoadedScenario()

        // When
        viewModel.onEvent(AnnotationsUiEvent.OnBackClick)

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = commands,
        )
    }

    @Test
    fun `GIVEN marks only in another version WHEN observing THEN shows nothing here and offers that version`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            otherVersionCounts.value = listOf(
                VersionAnnotationCount(
                    bibleVersionId = "a21",
                    count = 1,
                ),
            )
            entries.emit(emptyList())

            // Then
            val content = states
                .last()
                .content
                .valueOrNull()
            assertEquals(
                expected = 0,
                actual = content?.totalCount,
            )
            assertEquals(
                expected = listOf(
                    OtherVersionAnnotationsUiModel(
                        bibleVersionId = "a21",
                        versionAbbreviation = "A21",
                        count = 1,
                    ),
                ),
                actual = content?.otherVersions,
            )
        }

    @Test
    fun `GIVEN marks in other versions WHEN they change THEN lists the most marked version first`() =
        runTest(testDispatcher) {
            // Given
            prepareLoadedScenario()

            // When
            otherVersionCounts.value = listOf(
                VersionAnnotationCount(
                    bibleVersionId = "a21",
                    count = 1,
                ),
                VersionAnnotationCount(
                    bibleVersionId = "nvi",
                    count = 3,
                ),
            )

            // Then
            assertEquals(
                expected = listOf("nvi" to 3, "a21" to 1),
                actual = states
                    .last()
                    .content
                    .valueOrNull()
                    ?.otherVersions
                    ?.map { it.bibleVersionId to it.count },
            )
        }

    @Test
    fun `GIVEN an offered version WHEN choosing to use it THEN selects it and tracks where it was chosen`() =
        runTest(testDispatcher) {
            // Given
            prepareLoadedScenario()

            // When
            viewModel.onEvent(
                AnnotationsUiEvent.OnUseVersionClick(
                    bibleVersionId = "a21",
                    isEmptyState = true,
                ),
            )

            // Then
            assertEquals(
                expected = listOf("a21"),
                actual = selectedVersionIds,
            )
            assertEquals(
                expected = AnalyticsEventNames.ANNOTATIONS_OTHER_VERSION_USED to mapOf<String, Any>(
                    AnalyticsParams.VERSION_ID to "a21",
                    AnalyticsParams.SOURCE to "empty_state",
                ),
                actual = trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN an offered version above the list WHEN choosing to use it THEN tracks the list as the source`() =
        runTest(testDispatcher) {
            // Given
            prepareLoadedScenario()

            // When
            viewModel.onEvent(
                AnnotationsUiEvent.OnUseVersionClick(
                    bibleVersionId = "nvi",
                    isEmptyState = false,
                ),
            )

            // Then
            assertEquals(
                expected = "list",
                actual = trackedEvents.last().second[AnalyticsParams.SOURCE],
            )
        }

    private fun utcMillisOf(day: Int): Long = LocalDate(
        year = 2026,
        month = 9,
        day = day,
    ).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    private fun shownPassages(): List<AnnotatedPassage> = states
        .last()
        .content
        .valueOrNull()
        ?.groups
        ?.flatMap { group -> group.items.map { it.passage } }
        .orEmpty()

    private fun item(passage: AnnotatedPassage): AnnotationItemUiModel = states
        .last()
        .content
        .valueOrNull()
        ?.groups
        ?.flatMap { it.items }
        ?.first { it.passage == passage }
        ?: error("No item for $passage")

    private suspend fun TestScope.prepareLoadedScenario(
        isChapterRead: Boolean = false,
        isAddVerseNoteBlocked: Boolean = false,
    ) {
        prepareScenario(
            isChapterRead = isChapterRead,
            isAddVerseNoteBlocked = isAddVerseNoteBlocked,
        )
        entries.emit(listOf(highlightedPassage.toEntry(), notedPassage.toEntry()))
        runCurrent()
    }

    private fun TestScope.prepareScenario(
        isChapterRead: Boolean = false,
        isAddVerseNoteBlocked: Boolean = false,
    ) {
        val navigator = Navigator()
        entries = MutableSharedFlow(replay = 1)
        trackedEvents = mutableListOf()
        removedPassages = mutableListOf()
        otherVersionCounts = MutableStateFlow(emptyList())
        selectedVersionIds = mutableListOf()
        viewModel = AnnotationsViewModel(
            removePassageAnnotations = { passage -> removedPassages += passage },
            isWholeChapterRead = { _, _ -> isChapterRead },
            setSelectedVersion = { versionId -> selectedVersionIds += versionId },
            shouldBlockAddVerseNote = { isAddVerseNoteBlocked },
            getMaxFreeVerseNotesAmount = { MAX_FREE_VERSE_NOTES },
            navigator = navigator,
            platform = Platform.Android,
            contentFactory = AnnotationsContentFactory(
                currentTimestampProvider = { sampleNow },
                localDateTimeProvider = utcLocalDateTimeProvider,
            ),
            observeAnnotationEntries = { entries },
            observeOtherVersionAnnotationCounts = { otherVersionCounts },
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        states = mutableListOf<AnnotationsUiState>().also { collected ->
            backgroundScope.launch { viewModel.uiState.collect { collected += it } }
        }
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        actions = mutableListOf<AnnotationsUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }

    private companion object {
        const val MAX_FREE_VERSE_NOTES = 3
    }
}
