package com.quare.bibleplanner.feature.bookdetails.presentation.viewmodel

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.GetBookByIdFlowUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateBookReadStatusUseCase
import com.quare.bibleplanner.core.books.presentation.mapper.BookGroupMapper
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.review.domain.model.ReviewTrigger
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiEvent
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.utils.toSynopsisResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class BookDetailsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val defaultLocale: Locale = Locale.getDefault()
    private lateinit var database: AppDatabase
    private lateinit var viewModel: BookDetailsViewModel
    private lateinit var repository: FakeBooksRepository
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private lateinit var reviewTriggers: MutableStateFlow<List<ReviewTrigger>>
    private lateinit var loginNudgeRequests: MutableStateFlow<Int>

    private val genesis = BookDataModel(
        id = BookId.GEN,
        chapters = listOf(chapter(number = 1, isRead = true), chapter(number = 2, isRead = false)),
        isRead = false,
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        Locale.setDefault(Locale.US)
        database = Room
            .inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @AfterTest
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `GIVEN a partially read book WHEN opening THEN shows its progress and category`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = successState()

        // Then
        assertEquals(
            BookDetailsUiState.Success(
                id = BookId.GEN,
                nameStringResource = BookId.GEN.toBookNameResource(),
                synopsisStringResource = BookId.GEN.toSynopsisResource(),
                chapters = genesis.chapters,
                progress = 0.5f,
                readChaptersCount = 1,
                totalChaptersCount = 2,
                areAllChaptersRead = false,
                isFavorite = false,
                bookGroup = BookGroup.Pentateuch,
                bookCategoryName = "Pentateuch",
                isSynopsisExpanded = false,
            ),
            state,
        )
    }

    @Test
    fun `GIVEN a book without chapters WHEN opening THEN shows no progress and not completed`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(book = genesis.copy(chapters = emptyList()))

            // When
            val state = successState()

            // Then
            assertEquals(0f, state.progress)
            assertFalse(state.areAllChaptersRead)
        }

    @Test
    fun `GIVEN an unknown book WHEN opening THEN keeps loading`() = runTest(testDispatcher) {
        // Given
        prepareScenario(book = null)

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(BookDetailsUiState.Loading, state)
    }

    @Test
    fun `GIVEN the book is still loading WHEN interacting THEN ignores every action`() = runTest(testDispatcher) {
        // Given
        prepareScenario(book = null)

        // When
        sendEvent(BookDetailsUiEvent.OnToggleFavorite)
        sendEvent(BookDetailsUiEvent.OnToggleSynopsisExpanded)
        sendEvent(BookDetailsUiEvent.OnToggleAllChapters)

        // Then
        assertTrue(trackedEvents.isEmpty())
        assertTrue(repository.favoriteUpdates.isEmpty())
        assertEquals(BookDetailsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `GIVEN an unfinished book WHEN its last chapter is read THEN requests a review`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        repository.book.value = genesis.copy(chapters = genesis.chapters.map { it.copy(isRead = true) })
        val triggers = reviewTriggers.first(List<ReviewTrigger>::isNotEmpty)

        // Then
        assertEquals(listOf(ReviewTrigger.BOOK_COMPLETED), triggers)
    }

    @Test
    fun `GIVEN an already finished book WHEN opening THEN does not request a review`() = runTest(testDispatcher) {
        // Given
        prepareScenario(book = genesis.copy(chapters = genesis.chapters.map { it.copy(isRead = true) }))

        // When
        val state = successState()

        // Then
        assertTrue(state.areAllChaptersRead)
        assertTrue(reviewTriggers.value.isEmpty())
    }

    @Test
    fun `GIVEN a collapsed synopsis WHEN toggling it THEN expands it and tracks the change`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            sendEvent(BookDetailsUiEvent.OnToggleSynopsisExpanded)

            // Then
            assertTrue(successState().isSynopsisExpanded)
            assertEquals(
                AnalyticsEventNames.SYNOPSIS_TOGGLED to mapOf<String, Any>(
                    AnalyticsParams.BOOK_ID to "gen",
                    AnalyticsParams.IS_EXPANDED to true,
                ),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN an expanded synopsis WHEN the book changes THEN keeps the synopsis expanded`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            sendEvent(BookDetailsUiEvent.OnToggleSynopsisExpanded)

            // When
            repository.book.value = genesis.copy(isFavorite = true)
            viewModel.uiState.first { (it as? BookDetailsUiState.Success)?.isFavorite == true }

            // Then
            assertTrue(successState().isSynopsisExpanded)
        }

    @Test
    fun `GIVEN a book that is not a favorite WHEN toggling the favorite THEN saves it and requests the login nudge`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            sendEvent(BookDetailsUiEvent.OnToggleFavorite)

            // Then
            assertEquals(listOf(BookId.GEN to true), repository.favoriteUpdates)
            assertEquals(1, loginNudgeRequests.value)
            assertEquals(
                AnalyticsEventNames.BOOK_FAVORITE_TOGGLED to mapOf<String, Any>(
                    AnalyticsParams.BOOK_ID to "gen",
                    AnalyticsParams.IS_FAVORITE to true,
                    AnalyticsParams.SOURCE to "book_details",
                ),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN an unfinished book WHEN toggling all chapters THEN marks every chapter as read`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            seedGenesisChapters()

            // When
            sendEvent(BookDetailsUiEvent.OnToggleAllChapters)
            loginNudgeRequests.first { it == 1 }

            // Then
            assertTrue(database.chapterDao().getChaptersByBookId("GEN").all(ChapterEntity::isRead))
            assertEquals(
                AnalyticsEventNames.BOOK_READ_TOGGLED to mapOf<String, Any>(
                    AnalyticsParams.BOOK_ID to "gen",
                    AnalyticsParams.IS_READ to true,
                ),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN a finished book WHEN toggling all chapters THEN marks every chapter as unread`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(book = genesis.copy(chapters = genesis.chapters.map { it.copy(isRead = true) }))
            seedGenesisChapters()

            // When
            sendEvent(BookDetailsUiEvent.OnToggleAllChapters)
            loginNudgeRequests.first { it == 1 }

            // Then
            assertTrue(database.chapterDao().getChaptersByBookId("GEN").none(ChapterEntity::isRead))
        }

    @Test
    fun `GIVEN an unread chapter WHEN clicking it THEN opens the reader on that chapter`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        sendEvent(BookDetailsUiEvent.OnChapterClick(chapterNumber = 2))

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(
                    ReadNavRoute(
                        bookId = "GEN",
                        chapterNumber = 2,
                        isChapterRead = false,
                        isFromBookDetails = true,
                    ),
                ),
            ),
            commands,
        )
        assertEquals(
            AnalyticsEventNames.CHAPTER_CLICKED to mapOf<String, Any>(
                AnalyticsParams.CHAPTER_NUMBER to 2,
                AnalyticsParams.SOURCE to "book_details",
            ),
            trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN a chapter the book does not have WHEN clicking it THEN does not navigate`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        sendEvent(BookDetailsUiEvent.OnChapterClick(chapterNumber = 51))

        // Then
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN the details screen WHEN going back THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        sendEvent(BookDetailsUiEvent.OnBackClick)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(AnalyticsEventNames.BOOK_DETAILS_BACK_CLICKED to emptyMap(), trackedEvents.single())
    }

    private fun successState(): BookDetailsUiState.Success =
        assertIs<BookDetailsUiState.Success>(viewModel.uiState.value)

    private fun chapter(
        number: Int,
        isRead: Boolean,
    ): BookChapterModel = BookChapterModel(
        number = number,
        verses = emptyList(),
        isRead = isRead,
        readUpdatedAt = null,
    )

    private suspend fun seedGenesisChapters() {
        database.bookDao().insertBook(
            BookEntity(
                id = "GEN",
                favoriteUpdatedAt = null,
                isFavoritePendingSync = false,
            ),
        )
        database.chapterDao().insertChapters(
            repository.book.value
                ?.chapters
                .orEmpty()
                .map { chapter ->
                    ChapterEntity(
                        number = chapter.number,
                        bookId = "GEN",
                        isRead = chapter.isRead,
                    )
                },
        )
    }

    private fun TestScope.sendEvent(event: BookDetailsUiEvent) {
        viewModel.onEvent(event)
        runCurrent()
    }

    private suspend fun TestScope.prepareScenario(book: BookDataModel? = genesis) {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        reviewTriggers = MutableStateFlow(emptyList())
        loginNudgeRequests = MutableStateFlow(0)
        repository = FakeBooksRepository(book)
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        viewModel = BookDetailsViewModel(
            booksRepository = repository,
            markBookRead = UpdateBookReadStatusUseCase(
                bookDao = database.bookDao(),
                chapterDao = database.chapterDao(),
                verseDao = database.verseDao(),
                currentTimestampProvider = { TIMESTAMP },
                trackEvent = { _, _ -> },
            ),
            requestLoginNudgeIfNeeded = { loginNudgeRequests.value++ },
            navigator = navigator,
            platform = Platform.Android,
            route = BookDetailsNavRoute(BookId.GEN.name),
            bookGroupMapper = BookGroupMapper(),
            requestReviewIfNeeded = { trigger -> reviewTriggers.value += trigger },
            trackEvent = { name, params -> recordedEvents += name to params },
            getBookByIdFlow = GetBookByIdFlowUseCase(repository),
        )
        runCurrent()
        if (book != null) {
            viewModel.uiState.first { it is BookDetailsUiState.Success }
        }
    }

    private companion object {
        const val TIMESTAMP = 1_000L
    }
}

private class FakeBooksRepository(
    initialBook: BookDataModel?,
) : BooksRepository {
    val book = MutableStateFlow(initialBook)
    val favoriteUpdates = mutableListOf<Pair<BookId, Boolean>>()

    override fun getBooksFlow(): Flow<List<BookDataModel>> = error("unused")

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> = book

    override suspend fun getBooks(): List<BookDataModel> = error("unused")

    override suspend fun initializeDatabase() = error("unused")

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) {
        favoriteUpdates += bookId to isFavorite
    }

    override fun getBookLayoutFormatFlow(): Flow<String?> = error("unused")

    override suspend fun setBookLayoutFormat(layoutFormat: String) = error("unused")

    override fun getSelectedTestamentFlow(): Flow<String?> = error("unused")

    override suspend fun setSelectedTestament(testament: String) = error("unused")
}
