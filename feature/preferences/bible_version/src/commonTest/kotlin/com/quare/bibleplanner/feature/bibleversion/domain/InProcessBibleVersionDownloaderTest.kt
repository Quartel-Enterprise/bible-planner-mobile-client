package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadBooksInParallelUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadChaptersUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetNewTestamentIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPentateuchIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPrioritizedBookIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetRemoteContentVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleVersionRepository
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryBibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryChapterDao
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryVerseDao
import com.quare.bibleplanner.feature.bibleversion.fake.RecordingDownloadNotifier
import com.quare.bibleplanner.feature.bibleversion.fake.StorageServer
import com.quare.bibleplanner.feature.bibleversion.fake.bibleModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

internal class InProcessBibleVersionDownloaderTest {
    private val waitTimeout = 5.seconds
    private lateinit var downloader: InProcessBibleVersionDownloader
    private lateinit var bibleVersionDao: InMemoryBibleVersionDao
    private lateinit var notifier: RecordingDownloadNotifier
    private lateinit var finished: CompletableDeferred<String>

    @Test
    fun `marks the version in progress and notifies its completion by name`() = runTest {
        // Given
        prepareScenario()

        // When
        downloader.startDownload(VERSION_ID)
        val outcome = awaitReal(finished)

        // Then
        assertEquals(
            expected = "complete $VERSION_ID Version $VERSION_ID",
            actual = outcome,
        )
        assertEquals(
            expected = "progress $VERSION_ID Version $VERSION_ID 0.0",
            actual = notifier.calls.first(),
        )
        assertEquals(
            expected = VERSION_ID to DownloadStatus.IN_PROGRESS,
            actual = bibleVersionDao.statusUpdates.first(),
        )
    }

    @Test
    fun `notifies an error and pauses the version when the download fails`() = runTest {
        // Given
        prepareScenario()

        // When
        downloader.startDownload(UNKNOWN_VERSION_ID)
        val outcome = awaitReal(finished)

        // Then
        assertEquals(
            expected = "error $UNKNOWN_VERSION_ID $UNKNOWN_VERSION_ID",
            actual = outcome,
        )
        assertEquals(
            expected = listOf(
                UNKNOWN_VERSION_ID to DownloadStatus.IN_PROGRESS,
                UNKNOWN_VERSION_ID to DownloadStatus.PAUSED,
            ),
            actual = bibleVersionDao.statusUpdates,
        )
    }

    @Test
    fun `tracks a running download until it is cancelled`() = runTest {
        // Given
        prepareScenario(bibleRepository = NeverLoadingBibleRepository())
        downloader.startDownload(VERSION_ID)
        downloader.startDownload(VERSION_ID)
        val wasActive = downloader.hasActiveDownload(VERSION_ID) && downloader.hasAnyActiveDownload()

        // When
        downloader.cancelDownload(VERSION_ID)

        // Then
        assertTrue(wasActive)
        assertFalse(downloader.hasActiveDownload(VERSION_ID))
        assertFalse(downloader.hasAnyActiveDownload())
    }

    @Test
    fun `cancels every running download at once`() = runTest {
        // Given
        prepareScenario(bibleRepository = NeverLoadingBibleRepository())
        downloader.startDownload(VERSION_ID)
        downloader.startDownload(OTHER_VERSION_ID)

        // When
        downloader.cancelAllDownloads()

        // Then
        assertFalse(downloader.hasAnyActiveDownload())
    }

    @Test
    fun `resuming with nothing pending completes right away`() = runTest {
        // Given
        prepareScenario(versionStatus = DownloadStatus.DONE)
        val resumed = CompletableDeferred<Boolean>()

        // When
        downloader.resumePendingDownloads(resumed::complete)

        // Then
        assertTrue(awaitReal(resumed))
        assertTrue(notifier.calls.isEmpty())
    }

    @Test
    fun `resuming restarts the downloads left in progress and completes once they finish`() = runTest {
        // Given
        prepareScenario(versionStatus = DownloadStatus.IN_PROGRESS)
        val resumed = CompletableDeferred<Boolean>()

        // When
        downloader.resumePendingDownloads(resumed::complete)

        // Then
        assertTrue(awaitReal(resumed))
        assertTrue("complete $VERSION_ID Version $VERSION_ID" in notifier.calls)
    }

    private suspend fun <T> awaitReal(deferred: CompletableDeferred<T>): T = withContext(Dispatchers.Default) {
        withTimeout(waitTimeout) { deferred.await() }
    }

    private fun prepareScenario(
        versionStatus: DownloadStatus = DownloadStatus.DONE,
        bibleRepository: BibleRepository = FakeBibleRepository(listOf(bibleModel(VERSION_ID))),
    ) {
        finished = CompletableDeferred()
        notifier = RecordingDownloadNotifier { call ->
            if (call.startsWith("complete") || call.startsWith("error")) finished.complete(call)
        }
        bibleVersionDao = InMemoryBibleVersionDao(
            listOf(
                BibleVersionEntity(
                    id = VERSION_ID,
                    status = versionStatus,
                    totalChapters = 0,
                    contentVersion = "1.0.0",
                ),
            ),
        )
        val verseDao = InMemoryVerseDao()
        downloader = InProcessBibleVersionDownloader(
            bibleVersionDao = bibleVersionDao,
            downloadBible = DownloadBibleUseCase(
                bibleVersionDao = bibleVersionDao,
                verseDao = verseDao,
                getRemoteContentVersion = GetRemoteContentVersionUseCase(
                    FakeBibleVersionRepository(Result.success(emptyList())),
                ),
                downloadBooksInParallel = DownloadBooksInParallelUseCase(
                    getPrioritizedBookIds = GetPrioritizedBookIdsUseCase(
                        getPentateuchIds = GetPentateuchIdsUseCase(),
                        getNewTestamentIds = GetNewTestamentIdsUseCase(),
                    ),
                    downloadChapters = DownloadChaptersUseCase(
                        supabaseBookAbbreviationMapper = SupabaseBookAbbreviationMapper(),
                        chapterDao = InMemoryChapterDao(emptyList()),
                        verseDao = verseDao,
                        bucketApi = StorageServer(filesByPath = emptyMap()).bucketApi,
                    ),
                ),
                trackEvent = { _, _ -> },
            ),
            notifier = notifier,
            bibleRepository = bibleRepository,
            observeDownloadProgress = { flowOf() },
        )
    }

    private companion object {
        const val VERSION_ID = "acf"
        const val OTHER_VERSION_ID = "kjv"
        const val UNKNOWN_VERSION_ID = "missing"
    }
}

private class NeverLoadingBibleRepository : BibleRepository {
    private val bibles = MutableSharedFlow<List<BibleModel>>()

    override fun getBiblesFlow(): Flow<List<BibleModel>> = bibles

    override fun getSelectedVersionIdFlow(): Flow<String> = error("Unexpected call")

    override suspend fun setSelectedVersionId(id: String) = error("Unexpected call")
}
