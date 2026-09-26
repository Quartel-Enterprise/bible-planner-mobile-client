package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadBooksInParallelUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadChaptersUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetNewTestamentIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPentateuchIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPrioritizedBookIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetRemoteContentVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleVersionRepository
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryBibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryChapterDao
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryVerseDao
import com.quare.bibleplanner.feature.bibleversion.fake.StorageServer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DownloadBibleUseCaseTest {
    private lateinit var useCase: DownloadBibleUseCase
    private lateinit var bibleVersionDao: InMemoryBibleVersionDao
    private lateinit var server: StorageServer
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @Test
    fun `completes the download and stores the remote content version`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.IN_PROGRESS)

        // When
        val result = useCase(VERSION_ID)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = BibleVersionEntity(
                id = VERSION_ID,
                status = DownloadStatus.DONE,
                totalChapters = TOTAL_CHAPTERS,
                contentVersion = REMOTE_CONTENT_VERSION,
            ),
            actual = bibleVersionDao.versions[VERSION_ID],
        )
        assertEquals(
            expected = listOf(
                AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_COMPLETED to mapOf<String, Any>(
                    AnalyticsParams.VERSION_ID to VERSION_ID,
                ),
            ),
            actual = trackedEvents,
        )
    }

    @Test
    fun `keeps the local content version when the remote one is unknown`() = runTest {
        // Given
        prepareScenario(
            status = DownloadStatus.IN_PROGRESS,
            remoteVersions = Result.failure(IllegalStateException("offline")),
        )

        // When
        useCase(VERSION_ID)

        // Then
        assertEquals(
            expected = LOCAL_CONTENT_VERSION,
            actual = bibleVersionDao.versions[VERSION_ID]?.contentVersion,
        )
    }

    @Test
    fun `skips a version that is already fully downloaded`() = runTest {
        // Given
        prepareScenario(
            status = DownloadStatus.DONE,
            chaptersWithVerses = TOTAL_CHAPTERS,
        )

        // When
        val result = useCase(VERSION_ID)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(server.requestedPaths.isEmpty())
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `resumes a version marked done that is missing chapters`() = runTest {
        // Given
        prepareScenario(
            status = DownloadStatus.DONE,
            chaptersWithVerses = TOTAL_CHAPTERS - 1,
        )

        // When
        useCase(VERSION_ID)

        // Then
        assertEquals(
            expected = listOf(GENESIS_PATH),
            actual = server.requestedPaths,
        )
    }

    @Test
    fun `fails and tracks the reason when the version is unknown`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.IN_PROGRESS)

        // When
        val result = useCase("missing")

        // Then
        assertEquals(
            expected = "Version not found",
            actual = result.exceptionOrNull()?.message,
        )
        assertEquals(
            expected = listOf(
                AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_FAILED to mapOf<String, Any>(
                    AnalyticsParams.VERSION_ID to "missing",
                    AnalyticsParams.REASON to "IllegalStateException",
                ),
            ),
            actual = trackedEvents,
        )
    }

    @Test
    fun `fails and tracks the reason when a book fails to download`() = runTest {
        // Given
        prepareScenario(
            status = DownloadStatus.IN_PROGRESS,
            files = emptyMap(),
        )

        // When
        val result = useCase(VERSION_ID)

        // Then
        assertTrue(result.isFailure)
        assertEquals(
            expected = listOf(AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_FAILED),
            actual = trackedEvents.map { it.first },
        )
        assertEquals(
            expected = DownloadStatus.IN_PROGRESS,
            actual = bibleVersionDao.versions[VERSION_ID]?.status,
        )
    }

    private fun prepareScenario(
        status: DownloadStatus,
        chaptersWithVerses: Int = 0,
        remoteVersions: Result<List<VersionModel>> = Result.success(
            listOf(
                VersionModel(
                    id = VERSION_ID.uppercase(),
                    name = "Almeida Corrigida Fiel",
                    version = REMOTE_CONTENT_VERSION,
                    language = Language.PORTUGUESE_BRAZIL,
                    chapters = TOTAL_CHAPTERS,
                    size = null,
                ),
            ),
        ),
        files: Map<String, String> = mapOf(GENESIS_PATH to CHAPTER_JSON),
    ) {
        trackedEvents = mutableListOf()
        server = StorageServer(filesByPath = files)
        bibleVersionDao = InMemoryBibleVersionDao(
            listOf(
                BibleVersionEntity(
                    id = VERSION_ID,
                    status = status,
                    totalChapters = TOTAL_CHAPTERS,
                    contentVersion = LOCAL_CONTENT_VERSION,
                ),
            ),
        )
        val verseDao = InMemoryVerseDao(chaptersWithVerses = chaptersWithVerses)
        useCase = DownloadBibleUseCase(
            bibleVersionDao = bibleVersionDao,
            verseDao = verseDao,
            getRemoteContentVersion = GetRemoteContentVersionUseCase(FakeBibleVersionRepository(remoteVersions)),
            downloadBooksInParallel = DownloadBooksInParallelUseCase(
                getPrioritizedBookIds = GetPrioritizedBookIdsUseCase(
                    getPentateuchIds = GetPentateuchIdsUseCase(),
                    getNewTestamentIds = GetNewTestamentIdsUseCase(),
                ),
                downloadChapters = DownloadChaptersUseCase(
                    supabaseBookAbbreviationMapper = SupabaseBookAbbreviationMapper(),
                    chapterDao = InMemoryChapterDao(
                        listOf(
                            ChapterEntity(
                                id = 1L,
                                number = 1,
                                bookId = BookId.GEN.name,
                            ),
                        ),
                    ),
                    verseDao = verseDao,
                    bucketApi = server.bucketApi,
                ),
            ),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    private companion object {
        const val VERSION_ID = "acf"
        const val TOTAL_CHAPTERS = 1
        const val LOCAL_CONTENT_VERSION = "1.0.0"
        const val REMOTE_CONTENT_VERSION = "1.1.0"
        const val GENESIS_PATH = "bible/ACF/Gn/1.json"
        const val CHAPTER_JSON = """{"chapter":1,"verses":[]}"""
    }
}
