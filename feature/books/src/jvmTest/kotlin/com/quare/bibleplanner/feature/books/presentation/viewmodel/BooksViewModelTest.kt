package com.quare.bibleplanner.feature.books.presentation.viewmodel

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.GetBooksWithInformationBoxVisibilityUseCase
import com.quare.bibleplanner.core.books.domain.usecase.ToggleBookFavoriteUseCase
import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.feature.books.presentation.mapper.BookCategorizationMapper
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookSortOrder
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiAction
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiEvent
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class BooksViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val almostSearchDebounce = 999.milliseconds
    private lateinit var viewModel: BooksViewModel
    private lateinit var repository: FakeBooksRepository
    private lateinit var actions: List<BooksUiAction>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private var loginNudgeRequests = 0

    private val genesis = BookDataModel(
        id = BookId.GEN,
        chapters = listOf(chapter(isRead = true), chapter(isRead = true)),
        isRead = true,
    )
    private val exodus = BookDataModel(
        id = BookId.EXO,
        chapters = listOf(chapter(isRead = true), chapter(isRead = false)),
        isRead = false,
    )
    private val matthew = BookDataModel(
        id = BookId.MAT,
        chapters = listOf(chapter(isRead = false)),
        isRead = false,
        isFavorite = true,
    )
    private val john = BookDataModel(
        id = BookId.JHN,
        chapters = emptyList(),
        isRead = false,
    )

    private val defaultLocale: Locale = Locale.getDefault()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        Locale.setDefault(Locale.US)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `GIVEN books without persisted preferences WHEN opening THEN lists the old testament books in a list`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            val state = successState()

            // Then
            assertEquals(BookTestament.OldTestament, state.selectedTestament)
            assertEquals(BookLayoutFormat.List, state.layoutFormat)
            assertTrue(state.shouldShowTestamentToggle)
            assertEquals(
                listOf(
                    BookPresentationModel(
                        id = BookId.GEN,
                        name = "Genesis",
                        chapterProgressText = "2 / 2",
                        progress = 1f,
                        percentageText = "100%",
                        isCompleted = true,
                        isFavorite = false,
                    ),
                    BookPresentationModel(
                        id = BookId.EXO,
                        name = "Exodus",
                        chapterProgressText = "1 / 2",
                        progress = 0.5f,
                        percentageText = "50%",
                        isCompleted = false,
                        isFavorite = false,
                    ),
                ),
                state.filteredBooks,
            )
            assertEquals(listOf(BookGroup.Pentateuch), state.groupsInTestament.map { it.group })
        }

    @Test
    fun `GIVEN a book without chapters WHEN opening THEN shows it with no progress`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = successState()

        // Then
        val johnModel = state.books.single { it.id == BookId.JHN }
        assertEquals(0f, johnModel.progress)
        assertEquals("0 / 0", johnModel.chapterProgressText)
        assertFalse(johnModel.isCompleted)
    }

    @Test
    fun `GIVEN a persisted grid layout and new testament WHEN opening THEN restores them`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            layoutFormat = BookLayoutFormat.Grid.name,
            selectedTestament = BookTestament.NewTestament.name,
        )

        // When
        val state = successState()

        // Then
        assertEquals(BookLayoutFormat.Grid, state.layoutFormat)
        assertEquals(BookTestament.NewTestament, state.selectedTestament)
        assertEquals(listOf(BookId.MAT, BookId.JHN), state.filteredBooks.map { it.id })
    }

    @Test
    fun `GIVEN unknown persisted preferences WHEN opening THEN falls back to the defaults`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            layoutFormat = "Carousel",
            selectedTestament = "ApocryphalTestament",
        )

        // When
        val state = successState()

        // Then
        assertEquals(BookLayoutFormat.List, state.layoutFormat)
        assertEquals(BookTestament.OldTestament, state.selectedTestament)
    }

    @Test
    fun `GIVEN the books change WHEN they are emitted again THEN refreshes the list keeping the names`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            repository.books.value = listOf(genesis.copy(isFavorite = true), exodus, matthew, john)
            runCurrent()

            // Then
            val genesisModel = successState().books.first { it.id == BookId.GEN }
            assertTrue(genesisModel.isFavorite)
            assertEquals("Genesis", genesisModel.name)
        }

    @Test
    fun `GIVEN a search query WHEN typing THEN searches both testaments and scrolls to the top`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            sendEvent(BooksUiEvent.OnSearchQueryChange("MATT"))

            // Then
            val state = successState()
            assertEquals(listOf(BookId.MAT), state.filteredBooks.map { it.id })
            assertFalse(state.shouldShowTestamentToggle)
            assertEquals(listOf<BooksUiAction>(BooksUiAction.ScrollToTop), actions)
        }

    @Test
    fun `GIVEN a search query WHEN the typing settles THEN tracks the search once with the last query length`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            sendEvent(BooksUiEvent.OnSearchQueryChange("ge"))

            // When
            advanceTimeBy(almostSearchDebounce)
            sendEvent(BooksUiEvent.OnSearchQueryChange("gen"))
            advanceUntilIdle()

            // Then
            assertEquals(
                listOf(AnalyticsEventNames.BOOKS_SEARCH_USED to mapOf<String, Any>(AnalyticsParams.QUERY_LENGTH to 3)),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN a blank search query WHEN typing THEN does not track a search`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        sendEvent(BooksUiEvent.OnSearchQueryChange("  "))
        advanceUntilIdle()

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN an active search WHEN clearing it THEN shows the testament books again`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        sendEvent(BooksUiEvent.OnSearchQueryChange("zzz"))

        // When
        sendEvent(BooksUiEvent.OnClearSearch)

        // Then
        val state = successState()
        assertEquals("", state.searchQuery)
        assertTrue(state.shouldShowTestamentToggle)
        assertEquals(listOf(BookId.GEN, BookId.EXO), state.filteredBooks.map { it.id })
    }

    @Test
    fun `GIVEN the old testament WHEN selecting the new testament THEN persists it and tracks the switch`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            sendEvent(BooksUiEvent.OnTestamentSelect(BookTestament.NewTestament))

            // Then
            assertEquals(BookTestament.NewTestament, successState().selectedTestament)
            assertEquals(listOf(BookTestament.NewTestament.name), repository.selectedTestaments)
            assertEquals(listOf<BooksUiAction>(BooksUiAction.ScrollToTop), actions)
            assertEquals(
                AnalyticsEventNames.TESTAMENT_SWITCHED to mapOf<String, Any>(AnalyticsParams.TESTAMENT to "new"),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN a book WHEN clicking it THEN opens its details`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val book = successState().filteredBooks.first()

        // When
        sendEvent(BooksUiEvent.OnBookClick(book))

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(BookDetailsNavRoute("GEN"))), commands)
        assertEquals(
            AnalyticsEventNames.BOOK_CLICKED to mapOf<String, Any>(AnalyticsParams.BOOK_ID to "gen"),
            trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN no filter WHEN toggling only read THEN shows the completed books of every testament`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            sendEvent(BooksUiEvent.OnToggleFilterMenu)

            // When
            sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyRead))

            // Then
            val state = successState()
            assertEquals(listOf(BookId.GEN), state.filteredBooks.map { it.id })
            assertFalse(state.isFilterMenuVisible)
            assertEquals(
                listOf(BookFilterType.OnlyRead),
                state.filterOptions.filter { it.isSelected }.map { it.type },
            )
            assertEquals(
                AnalyticsEventNames.BOOKS_FILTER_TOGGLED to mapOf<String, Any>(
                    AnalyticsParams.FILTER_TYPE to "only_read",
                    AnalyticsParams.IS_ACTIVE to true,
                ),
                trackedEvents.last(),
            )
            assertEquals(listOf<BooksUiAction>(BooksUiAction.ScrollToTop), actions)
        }

    @Test
    fun `GIVEN only read active WHEN toggling only unread THEN replaces it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyRead))

        // When
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyUnread))

        // Then
        val state = successState()
        assertEquals(listOf(BookId.EXO, BookId.MAT, BookId.JHN), state.filteredBooks.map { it.id })
        assertEquals(
            listOf(BookFilterType.OnlyUnread),
            state.filterOptions.filter { it.isSelected }.map { it.type },
        )
    }

    @Test
    fun `GIVEN only unread active WHEN toggling only read THEN replaces it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyUnread))

        // When
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyRead))

        // Then
        assertEquals(listOf(BookId.GEN), successState().filteredBooks.map { it.id })
    }

    @Test
    fun `GIVEN a filter active WHEN toggling it again THEN removes it and tracks it as inactive`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyUnread))

            // When
            sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyUnread))

            // Then
            val state = successState()
            assertTrue(state.filterOptions.none { it.isSelected })
            assertTrue(state.shouldShowTestamentToggle)
            assertEquals(
                AnalyticsEventNames.BOOKS_FILTER_TOGGLED to mapOf<String, Any>(
                    AnalyticsParams.FILTER_TYPE to "only_unread",
                    AnalyticsParams.IS_ACTIVE to false,
                ),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN only read active WHEN toggling only read again THEN removes it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyRead))

        // When
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyRead))

        // Then
        assertTrue(successState().filterOptions.none { it.isSelected })
    }

    @Test
    fun `GIVEN only unread active WHEN toggling favorites THEN combines both filters`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.OnlyUnread))

        // When
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.Favorites))

        // Then
        assertEquals(listOf(BookId.MAT), successState().filteredBooks.map { it.id })
        assertEquals(
            AnalyticsEventNames.BOOKS_FILTER_TOGGLED to mapOf<String, Any>(
                AnalyticsParams.FILTER_TYPE to "favorites",
                AnalyticsParams.IS_ACTIVE to true,
            ),
            trackedEvents.last(),
        )
    }

    @Test
    fun `GIVEN favorites active WHEN toggling favorites again THEN removes it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.Favorites))

        // When
        sendEvent(BooksUiEvent.OnToggleFilter(BookFilterType.Favorites))

        // Then
        assertTrue(successState().filterOptions.none { it.isSelected })
    }

    @Test
    fun `GIVEN a favorite book WHEN toggling its favorite THEN unfavorites it and requests the login nudge`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            sendEvent(BooksUiEvent.OnToggleFavorite(BookId.MAT))

            // Then
            assertEquals(listOf(BookId.MAT to false), repository.favoriteUpdates)
            assertEquals(1, loginNudgeRequests)
            assertEquals(
                AnalyticsEventNames.BOOK_FAVORITE_TOGGLED to mapOf<String, Any>(
                    AnalyticsParams.BOOK_ID to "mat",
                    AnalyticsParams.IS_FAVORITE to false,
                    AnalyticsParams.SOURCE to "books_list",
                ),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN a book that is not listed WHEN toggling its favorite THEN does nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        sendEvent(BooksUiEvent.OnToggleFavorite(BookId.REV))

        // Then
        assertTrue(repository.favoriteUpdates.isEmpty())
        assertEquals(0, loginNudgeRequests)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a closed filter menu WHEN toggling and then dismissing it THEN opens and closes it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            sendEvent(BooksUiEvent.OnToggleFilterMenu)
            val openedState = successState()

            // When
            sendEvent(BooksUiEvent.OnDismissFilterMenu)

            // Then
            assertTrue(openedState.isFilterMenuVisible)
            assertFalse(successState().isFilterMenuVisible)
        }

    @Test
    fun `GIVEN a closed sort menu WHEN toggling and then dismissing it THEN opens and closes it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            sendEvent(BooksUiEvent.OnToggleSortMenu)
            val openedState = successState()

            // When
            sendEvent(BooksUiEvent.OnDismissSortMenu)

            // Then
            assertTrue(openedState.isSortMenuVisible)
            assertFalse(successState().isSortMenuVisible)
        }

    @Test
    fun `GIVEN no sort WHEN sorting ascending THEN sorts every book by name and tracks it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        sendEvent(BooksUiEvent.OnToggleSortMenu)

        // When
        sendEvent(BooksUiEvent.OnSortOrderSelect(BookSortOrder.AlphabeticalAscending))

        // Then
        val state = successState()
        assertEquals(listOf(BookId.EXO, BookId.GEN, BookId.JHN, BookId.MAT), state.filteredBooks.map { it.id })
        assertEquals(BookSortOrder.AlphabeticalAscending, state.sortOrder)
        assertFalse(state.isSortMenuVisible)
        assertFalse(state.shouldShowTestamentToggle)
        assertEquals(
            AnalyticsEventNames.BOOKS_SORT_CHANGED to
                mapOf<String, Any>(AnalyticsParams.SORT_ORDER to "alphabetical_ascending"),
            trackedEvents.last(),
        )
        assertEquals(listOf<BooksUiAction>(BooksUiAction.ScrollToTop), actions)
    }

    @Test
    fun `GIVEN no sort WHEN sorting descending THEN sorts every group by name descending`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        sendEvent(BooksUiEvent.OnSortOrderSelect(BookSortOrder.AlphabeticalDescending))

        // Then
        val state = successState()
        assertEquals(listOf(BookId.MAT, BookId.JHN, BookId.GEN, BookId.EXO), state.filteredBooks.map { it.id })
        assertEquals(
            listOf(BookId.GEN, BookId.EXO),
            state.groupsInTestament
                .single()
                .books
                .map { it.id },
        )
        assertEquals(
            AnalyticsEventNames.BOOKS_SORT_CHANGED to
                mapOf<String, Any>(AnalyticsParams.SORT_ORDER to "alphabetical_descending"),
            trackedEvents.last(),
        )
    }

    @Test
    fun `GIVEN an ascending sort WHEN sorting ascending again THEN clears the sort without tracking`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            sendEvent(BooksUiEvent.OnSortOrderSelect(BookSortOrder.AlphabeticalAscending))

            // When
            sendEvent(BooksUiEvent.OnSortOrderSelect(BookSortOrder.AlphabeticalAscending))

            // Then
            val state = successState()
            assertEquals(null, state.sortOrder)
            assertEquals(listOf(BookId.GEN, BookId.EXO), state.filteredBooks.map { it.id })
            assertEquals(1, trackedEvents.count { it.first == AnalyticsEventNames.BOOKS_SORT_CHANGED })
        }

    @Test
    fun `GIVEN the list layout WHEN selecting the grid layout THEN persists it and scrolls to the top`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            sendEvent(BooksUiEvent.OnLayoutFormatSelect(BookLayoutFormat.Grid))

            // Then
            assertEquals(BookLayoutFormat.Grid, successState().layoutFormat)
            assertEquals(listOf(BookLayoutFormat.Grid.name), repository.layoutFormats)
            assertEquals(listOf<BooksUiAction>(BooksUiAction.ScrollToTop), actions)
        }

    @Test
    fun `GIVEN a web app url WHEN clicking the web app link THEN opens it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        sendEvent(BooksUiEvent.OnWebAppLinkClick)

        // Then
        assertEquals(listOf<BooksUiAction>(BooksUiAction.OpenWebAppLink(WEB_APP_URL)), actions)
    }

    private fun successState(): BooksUiState.Success = assertIs<BooksUiState.Success>(viewModel.uiState.value)

    private fun chapter(isRead: Boolean): BookChapterModel = BookChapterModel(
        number = 1,
        verses = emptyList(),
        isRead = isRead,
        readUpdatedAt = null,
    )

    private fun TestScope.sendEvent(event: BooksUiEvent) {
        viewModel.onEvent(event)
        runCurrent()
    }

    private suspend fun TestScope.prepareScenario(
        layoutFormat: String? = null,
        selectedTestament: String? = null,
    ) {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        loginNudgeRequests = 0
        repository = FakeBooksRepository(
            initialBooks = listOf(genesis, exodus, matthew, john),
            initialLayoutFormat = layoutFormat,
            initialSelectedTestament = selectedTestament,
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        val bookGroupMapper = BookGroupMapper()
        viewModel = BooksViewModel(
            booksRepository = repository,
            toggleBookFavorite = ToggleBookFavoriteUseCase(repository),
            getWebAppUrl = { WEB_APP_URL },
            bookGroupMapper = bookGroupMapper,
            bookCategorizationMapper = BookCategorizationMapper(bookGroupMapper),
            requestLoginNudgeIfNeeded = { loginNudgeRequests++ },
            navigator = navigator,
            trackEvent = { name, params -> recordedEvents += name to params },
            getBooksWithInformationBoxVisibility = GetBooksWithInformationBoxVisibilityUseCase(repository),
        )
        viewModel.uiState.first { it is BooksUiState.Success }
        actions = mutableListOf<BooksUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        runCurrent()
    }

    private companion object {
        const val WEB_APP_URL = "https://bibleplanner.app"
    }
}

private class FakeBooksRepository(
    initialBooks: List<BookDataModel>,
    initialLayoutFormat: String?,
    initialSelectedTestament: String?,
) : BooksRepository {
    val books = MutableStateFlow(initialBooks)
    private val layoutFormat = MutableStateFlow(initialLayoutFormat)
    private val selectedTestament = MutableStateFlow(initialSelectedTestament)
    val favoriteUpdates = mutableListOf<Pair<BookId, Boolean>>()
    val layoutFormats = mutableListOf<String>()
    val selectedTestaments = mutableListOf<String>()

    override fun getBooksFlow(): Flow<List<BookDataModel>> = books

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> = error("unused")

    override suspend fun getBooks(): List<BookDataModel> = error("unused")

    override suspend fun initializeDatabase() = error("unused")

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) {
        favoriteUpdates += bookId to isFavorite
    }

    override fun getBookLayoutFormatFlow(): Flow<String?> = layoutFormat

    override suspend fun setBookLayoutFormat(layoutFormat: String) {
        layoutFormats += layoutFormat
    }

    override fun getSelectedTestamentFlow(): Flow<String?> = selectedTestament

    override suspend fun setSelectedTestament(testament: String) {
        selectedTestaments += testament
    }
}
