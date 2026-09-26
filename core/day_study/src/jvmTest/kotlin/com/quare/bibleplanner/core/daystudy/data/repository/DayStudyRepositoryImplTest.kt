package com.quare.bibleplanner.core.daystudy.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.db.DatabaseConstructor
import com.quare.bibleplanner.core.daystudy.data.datasource.DayStudyLocalDataSource
import com.quare.bibleplanner.core.daystudy.data.datasource.DayStudyRemoteDataSource
import com.quare.bibleplanner.core.daystudy.data.dto.ChapterRequestDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyRequestDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyStatusDto
import com.quare.bibleplanner.core.daystudy.data.dto.PassageRequestDto
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyCacheKeyFactory
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyContentMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyEntityMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyPhaseMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyRequestMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyStatusMapper
import com.quare.bibleplanner.core.daystudy.data.model.DayStudyStreamEvent
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.core.daystudy.domain.usecase.ClearDayStudyLocalDataUseCase
import com.quare.bibleplanner.core.daystudy.fake.dayStudyModel
import com.quare.bibleplanner.core.daystudy.fake.dayStudyResponse
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayStudyRepositoryImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val passages = listOf(
        PassageModel(
            bookId = BookId.GEN,
            chapters = listOf(
                ChapterModel(
                    number = 1,
                    startVerse = null,
                    endVerse = null,
                    bookId = BookId.GEN,
                ),
            ),
            isRead = false,
            chapterRanges = "1",
        ),
    )
    private lateinit var repository: DayStudyRepositoryImpl
    private lateinit var remoteDataSource: FakeDayStudyRemoteDataSource
    private lateinit var localDataSource: DayStudyLocalDataSource
    private lateinit var database: AppDatabase

    @BeforeTest
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder<AppDatabase>(DatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(testDispatcher)
            .build()
        remoteDataSource = FakeDayStudyRemoteDataSource()
        localDataSource = DayStudyLocalDataSource(database.dayStudyDao())
        repository = DayStudyRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            requestMapper = DayStudyRequestMapper(BookIdWireNameMapper()),
            cacheKeyFactory = DayStudyCacheKeyFactory(),
            contentMapper = DayStudyContentMapper(),
            entityMapper = DayStudyEntityMapper(),
            statusMapper = DayStudyStatusMapper(),
            phaseMapper = DayStudyPhaseMapper(),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN no local study WHEN generating THEN emits known phases and the study and keeps a local copy`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.events = listOf(
                DayStudyStreamEvent.Progress(phase = "reading"),
                DayStudyStreamEvent.Progress(phase = "polishing"),
                DayStudyStreamEvent.Progress(phase = "questions"),
                DayStudyStreamEvent.Complete(dayStudyResponse(cacheToken = "token-1")),
            )

            // When
            val events = getDayStudy().toList()

            // Then
            assertEquals(
                listOf(
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.QUESTIONS),
                    DayStudyGenerationEventModel.Completed(dayStudyModel),
                ),
                events,
            )
            assertTrue(hasCachedStudy())
            assertEquals(
                DayStudyRequestDto(
                    passages = listOf(
                        PassageRequestDto(
                            book = "GENESIS",
                            chapters = listOf(
                                ChapterRequestDto(
                                    number = 1,
                                    startVerse = null,
                                    endVerse = null,
                                ),
                            ),
                        ),
                    ),
                    version = "ACF",
                    language = "en",
                ),
                remoteDataSource.streamedRequests.single(),
            )
        }

    @Test
    fun `GIVEN a local study WHEN opening it THEN emits it without calling the server`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.events = listOf(DayStudyStreamEvent.Complete(dayStudyResponse(cacheToken = "token-1")))
        getDayStudy().toList()

        // When
        val events = getDayStudy().toList()

        // Then
        assertEquals(
            listOf<DayStudyGenerationEventModel>(DayStudyGenerationEventModel.Completed(dayStudyModel)),
            events,
        )
        assertEquals(1, remoteDataSource.streamedRequests.size)
    }

    @Test
    fun `GIVEN a failing stream WHEN generating THEN propagates the failure`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.streamError = IllegalStateException("stream broke")

        // When
        val failure = assertFailsWith<IllegalStateException> { getDayStudy().toList() }

        // Then
        assertEquals("stream broke", failure.message)
        assertFalse(hasCachedStudy())
    }

    @Test
    fun `GIVEN a server status WHEN reading it THEN returns the mapped quota`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.status = Result.success(statusDto(cacheToken = "token-1"))

        // When
        val status = getStatus()

        // Then
        assertEquals(
            DayStudyStatusModel(
                freeLimit = 3,
                usedCount = 1,
                isUnlocked = true,
                cacheToken = "token-1",
            ),
            status,
        )
    }

    @Test
    fun `GIVEN a local copy with an outdated token WHEN reading the status THEN drops the local copy`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.events = listOf(DayStudyStreamEvent.Complete(dayStudyResponse(cacheToken = "old-token")))
            getDayStudy().toList()
            remoteDataSource.status = Result.success(statusDto(cacheToken = "new-token"))

            // When
            getStatus()

            // Then
            assertFalse(hasCachedStudy())
        }

    @Test
    fun `GIVEN a local copy with the current token WHEN reading the status THEN keeps the local copy`() =
        runTest(testDispatcher) {
            // Given
            remoteDataSource.events = listOf(DayStudyStreamEvent.Complete(dayStudyResponse(cacheToken = "token-1")))
            getDayStudy().toList()
            remoteDataSource.status = Result.success(statusDto(cacheToken = "token-1"))

            // When
            getStatus()

            // Then
            assertTrue(hasCachedStudy())
        }

    @Test
    fun `GIVEN the status request fails WHEN reading the status THEN returns null`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.status = Result.failure(IllegalStateException("offline"))

        // When
        val status = getStatus()

        // Then
        assertNull(status)
    }

    @Test
    fun `GIVEN local studies WHEN clearing the local data THEN removes them all`() = runTest(testDispatcher) {
        // Given
        remoteDataSource.events = listOf(DayStudyStreamEvent.Complete(dayStudyResponse(cacheToken = "token-1")))
        getDayStudy().toList()

        // When
        ClearDayStudyLocalDataUseCase(localDataSource)()

        // Then
        assertFalse(hasCachedStudy())
    }

    private fun getDayStudy(): Flow<DayStudyGenerationEventModel> = repository.getDayStudy(
        passages = passages,
        version = "ACF",
        languageCode = "en",
    )

    private suspend fun hasCachedStudy(): Boolean = repository.hasCachedStudy(
        passages = passages,
        version = "ACF",
        languageCode = "en",
    )

    private suspend fun getStatus(): DayStudyStatusModel? = repository.getDayStudyStatus(
        passages = passages,
        version = "ACF",
        languageCode = "en",
    )

    private fun statusDto(cacheToken: String): DayStudyStatusDto = DayStudyStatusDto(
        isUnlocked = true,
        usedCount = 1,
        freeLimit = 3,
        isPro = false,
        clientCacheToken = cacheToken,
    )
}

private class FakeDayStudyRemoteDataSource : DayStudyRemoteDataSource {
    var events: List<DayStudyStreamEvent> = emptyList()
    var streamError: Throwable? = null
    var status: Result<DayStudyStatusDto> = Result.failure(IllegalStateException("unset"))
    val streamedRequests = mutableListOf<DayStudyRequestDto>()

    override fun streamDayStudy(request: DayStudyRequestDto): Flow<DayStudyStreamEvent> = flow {
        streamedRequests += request
        streamError?.let { throw it }
        events.forEach { emit(it) }
    }

    override suspend fun fetchStatus(request: DayStudyRequestDto): Result<DayStudyStatusDto> = status
}
