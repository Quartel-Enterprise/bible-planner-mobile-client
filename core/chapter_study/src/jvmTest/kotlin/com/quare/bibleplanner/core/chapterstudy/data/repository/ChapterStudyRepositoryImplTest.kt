package com.quare.bibleplanner.core.chapterstudy.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.chapterstudy.data.datasource.ChapterStudyLocalDataSource
import com.quare.bibleplanner.core.chapterstudy.data.datasource.ChapterStudyRemoteDataSource
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyRequestDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyStatusDto
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyCacheKeyFactory
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyEntityMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyPhaseMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyRequestMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyStatusMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.WireNameBookIdMapper
import com.quare.bibleplanner.core.chapterstudy.data.model.ChapterStudyStreamEvent
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyPhaseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.fake.chapterStudyResponse
import com.quare.bibleplanner.core.chapterstudy.testing.createChapterStudy
import com.quare.bibleplanner.core.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.db.DatabaseConstructor
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterStudyRepositoryImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val chapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private lateinit var repository: ChapterStudyRepositoryImpl
    private lateinit var remoteDataSource: FakeChapterStudyRemoteDataSource
    private lateinit var database: AppDatabase

    @BeforeTest
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder<AppDatabase>(DatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(testDispatcher)
            .build()
        remoteDataSource = FakeChapterStudyRemoteDataSource()
        val bookIdWireNameMapper = BookIdWireNameMapper()
        repository = ChapterStudyRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = ChapterStudyLocalDataSource(database.chapterStudyDao()),
            requestMapper = ChapterStudyRequestMapper(bookIdWireNameMapper),
            cacheKeyFactory = ChapterStudyCacheKeyFactory(),
            entityMapper = ChapterStudyEntityMapper(WireNameBookIdMapper(bookIdWireNameMapper)),
            statusMapper = ChapterStudyStatusMapper(),
            phaseMapper = ChapterStudyPhaseMapper(),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN no local study WHEN generating THEN emits the known phases then the study`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.events = listOf(
            ChapterStudyStreamEvent.Progress(phase = "reading"),
            ChapterStudyStreamEvent.Progress(phase = "polishing"),
            ChapterStudyStreamEvent.Progress(phase = "questions"),
            ChapterStudyStreamEvent.Complete(chapterStudyResponse(cacheToken = "token-1")),
        )

        // When
        val events = generateChapterStudy().toList()

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyGenerationEventModel.PhaseChanged(ChapterStudyPhaseModel.READING),
                ChapterStudyGenerationEventModel.PhaseChanged(ChapterStudyPhaseModel.QUESTIONS),
                ChapterStudyGenerationEventModel.Completed(createChapterStudy()),
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a chapter WHEN generating THEN streams the request of that chapter`() = runTest(testDispatcher) {
        // When
        generateChapterStudy().toList()

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyRequestDto(
                    book = "GENESIS",
                    chapter = 3,
                    version = "ACF",
                    language = "en",
                    reward = false,
                ),
            ),
            actual = remoteDataSource.streamedRequests,
        )
    }

    @Test
    fun `GIVEN a generated study WHEN finding the cached study THEN returns the stored copy`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.events = listOf(
                ChapterStudyStreamEvent.Complete(chapterStudyResponse(cacheToken = "token-1")),
            )
            generateChapterStudy().toList()

            // When
            val cachedStudy = findCachedStudy()

            // Then
            assertEquals(
                expected = createChapterStudy(),
                actual = cachedStudy,
            )
        }

    @Test
    fun `GIVEN a study of another language WHEN finding the cached study THEN finds nothing`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.events = listOf(
                ChapterStudyStreamEvent.Complete(chapterStudyResponse(cacheToken = "token-1")),
            )
            generateChapterStudy().toList()

            // When
            val cachedStudy = repository.findCachedStudy(
                chapter = chapter,
                languageCode = "es",
            )

            // Then
            assertNull(cachedStudy)
        }

    @Test
    fun `GIVEN the stream answers 402 WHEN generating THEN fails with LimitReachedException`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.streamError = SSEClientException(response = paymentRequiredResponse())

            // When
            val result = runCatching { generateChapterStudy().toList() }

            // Then
            assertIs<LimitReachedException>(result.exceptionOrNull())
            assertNull(findCachedStudy())
        }

    @Test
    fun `GIVEN a 402 rest failure WHEN generating THEN fails with LimitReachedException`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.streamError = RestException(
            error = "Limit reached",
            description = null,
            response = paymentRequiredResponse(),
        )

        // When
        val result = runCatching { generateChapterStudy().toList() }

        // Then
        assertIs<LimitReachedException>(result.exceptionOrNull())
    }

    @Test
    fun `GIVEN a failing stream WHEN generating THEN propagates the failure`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.streamError = IllegalStateException("stream broke")

        // When
        val failure = assertFailsWith<IllegalStateException> { generateChapterStudy().toList() }

        // Then
        assertEquals(
            expected = "stream broke",
            actual = failure.message,
        )
        assertNull(findCachedStudy())
    }

    @Test
    fun `GIVEN a server status WHEN fetching it THEN returns the mapped quota`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.status = Result.success(statusDto(cacheToken = "token-1"))

        // When
        val status = fetchStatus()

        // Then
        assertEquals(
            expected = ChapterStudyStatusModel(
                freeLimit = 3,
                usedCount = 1,
                isUnlocked = true,
                cacheToken = "token-1",
                rewardedRemainingToday = 2,
            ),
            actual = status,
        )
        assertEquals(
            expected = listOf(
                ChapterStudyRequestDto(
                    book = "GENESIS",
                    chapter = 3,
                    version = "ACF",
                    language = "en",
                    reward = false,
                ),
            ),
            actual = remoteDataSource.statusRequests,
        )
    }

    @Test
    fun `GIVEN a local copy with an outdated token WHEN fetching the status THEN drops the local copy`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.events = listOf(
                ChapterStudyStreamEvent.Complete(chapterStudyResponse(cacheToken = "old-token")),
            )
            generateChapterStudy().toList()
            remoteDataSource.status = Result.success(statusDto(cacheToken = "new-token"))

            // When
            fetchStatus()

            // Then
            assertNull(findCachedStudy())
        }

    @Test
    fun `GIVEN a local copy with the current token WHEN fetching the status THEN keeps the local copy`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.events = listOf(
                ChapterStudyStreamEvent.Complete(chapterStudyResponse(cacheToken = "token-1")),
            )
            generateChapterStudy().toList()
            remoteDataSource.status = Result.success(statusDto(cacheToken = "token-1"))

            // When
            fetchStatus()

            // Then
            assertEquals(
                expected = createChapterStudy(),
                actual = findCachedStudy(),
            )
        }

    @Test
    fun `GIVEN the status request fails WHEN fetching the status THEN returns null and keeps the local copy`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.events = listOf(
                ChapterStudyStreamEvent.Complete(chapterStudyResponse(cacheToken = "token-1")),
            )
            generateChapterStudy().toList()
            remoteDataSource.status = Result.failure(IllegalStateException("offline"))

            // When
            val status = fetchStatus()

            // Then
            assertNull(status)
            assertEquals(
                expected = createChapterStudy(),
                actual = findCachedStudy(),
            )
        }

    @Test
    fun `GIVEN local studies WHEN clearing the cache THEN removes them all`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.events = listOf(
            ChapterStudyStreamEvent.Complete(chapterStudyResponse(cacheToken = "token-1")),
        )
        generateChapterStudy().toList()

        // When
        repository.clearCache()

        // Then
        assertNull(findCachedStudy())
    }

    private fun generateChapterStudy(): Flow<ChapterStudyGenerationEventModel> = repository.generateChapterStudy(
        chapter = chapter,
        languageCode = "en",
        isRewarded = false,
    )

    private suspend fun findCachedStudy(): ChapterStudyModel? = repository.findCachedStudy(
        chapter = chapter,
        languageCode = "en",
    )

    private suspend fun fetchStatus(): ChapterStudyStatusModel? = repository.fetchStatus(
        chapter = chapter,
        languageCode = "en",
    )

    // The engine answers on the calling thread: a real thread hop inside runTest lets the virtual
    // clock expire the Room connection timeout while the database is still being opened.
    private suspend fun paymentRequiredResponse(): HttpResponse = HttpClient(
        MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                addHandler {
                    respond(
                        content = """{"error":"Limit reached","code":"LIMIT_EXCEEDED","limit":3}""",
                        status = HttpStatusCode.PaymentRequired,
                    )
                }
            },
        ),
    ).post("https://project.supabase.co/functions/v1/get-chapter-study")

    private fun statusDto(cacheToken: String): ChapterStudyStatusDto = ChapterStudyStatusDto(
        isUnlocked = true,
        usedCount = 1,
        freeLimit = 3,
        isPro = false,
        clientCacheToken = cacheToken,
        rewardedRemainingToday = 2,
    )
}

private class FakeChapterStudyRemoteDataSource : ChapterStudyRemoteDataSource {
    var events: List<ChapterStudyStreamEvent> = emptyList()
    var streamError: Throwable? = null
    var status: Result<ChapterStudyStatusDto> = Result.failure(IllegalStateException("unset"))
    val streamedRequests = mutableListOf<ChapterStudyRequestDto>()
    val statusRequests = mutableListOf<ChapterStudyRequestDto>()

    override fun streamChapterStudy(request: ChapterStudyRequestDto): Flow<ChapterStudyStreamEvent> = flow {
        streamedRequests += request
        streamError?.let { throw it }
        events.forEach { emit(it) }
    }

    override suspend fun fetchStatus(request: ChapterStudyRequestDto): Result<ChapterStudyStatusDto> {
        statusRequests += request
        return status
    }
}
