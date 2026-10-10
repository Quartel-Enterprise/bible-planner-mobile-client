package com.quare.bibleplanner.feature.bibleversion.domain

import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeededUseCase
import com.quare.bibleplanner.core.books.testing.FakeBibleVersionRepository
import com.quare.bibleplanner.core.books.testing.FakeBooksRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.feature.bibleversion.data.mapper.SupabaseBookAbbreviationMapper
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadBooksInParallelUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DownloadChaptersUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetNewTestamentIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPentateuchIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPrioritizedBookIdsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetRemoteContentVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.PauseBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryBibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryChapterDao
import com.quare.bibleplanner.feature.bibleversion.fake.NeverLoadingBibleRepository
import com.quare.bibleplanner.feature.bibleversion.fake.NoOpDeleteVerseDao
import com.quare.bibleplanner.feature.bibleversion.fake.RecordingDownloadNotifier
import com.quare.bibleplanner.feature.bibleversion.fake.StorageServer
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class InProcessBibleVersionDownloaderFacadeTest {
    private lateinit var facade: InProcessBibleVersionDownloaderFacade
    private lateinit var downloader: InProcessBibleVersionDownloader
    private lateinit var bibleVersionDao: InMemoryBibleVersionDao
    private lateinit var notifier: RecordingDownloadNotifier

    @BeforeTest
    fun setUp() {
        notifier = RecordingDownloadNotifier()
        bibleVersionDao = InMemoryBibleVersionDao(
            listOf(
                BibleVersionEntity(
                    id = VERSION_ID,
                    status = DownloadStatus.IN_PROGRESS,
                    totalChapters = 0,
                    contentVersion = "1.0.0",
                ),
            ),
        )
        val verseDao = NoOpDeleteVerseDao()
        downloader = InProcessBibleVersionDownloader(
            bibleVersionDao = bibleVersionDao,
            downloadBible = DownloadBibleUseCase(
                bibleVersionDao = bibleVersionDao,
                verseDao = verseDao,
                initializeBooksIfNeeded = InitializeBooksIfNeededUseCase(FakeBooksRepository(emptyList())),
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
            bibleRepository = NeverLoadingBibleRepository(),
            observeDownloadProgress = { flowOf() },
        )
        facade = InProcessBibleVersionDownloaderFacade(
            downloader = downloader,
            pauseBibleVersion = PauseBibleVersionDownloadUseCase(
                bibleVersionDao = bibleVersionDao,
                notifier = notifier,
            ),
            deleteBibleVersion = DeleteBibleVersionDownloadUseCase(
                bibleVersionDao = bibleVersionDao,
                verseDao = verseDao,
                notifier = notifier,
            ),
        )
    }

    @Test
    fun `GIVEN an in-process downloader WHEN asking for the download tip THEN never shows it`() {
        // When
        val shouldShowDownloadTip = facade.shouldShowDownloadTip

        // Then
        assertFalse(shouldShowDownloadTip)
    }

    @Test
    fun `GIVEN a version WHEN downloading it THEN the download runs in process`() {
        // When
        facade.downloadVersion(VERSION_ID)

        // Then
        assertTrue(downloader.hasActiveDownload(VERSION_ID))
        downloader.cancelAllDownloads()
    }

    @Test
    fun `GIVEN a running download WHEN pausing it THEN cancels it and marks the version paused`() = runTest {
        // Given
        facade.downloadVersion(VERSION_ID)

        // When
        facade.pauseDownload(VERSION_ID)

        // Then
        assertFalse(downloader.hasActiveDownload(VERSION_ID))
        assertEquals(
            expected = DownloadStatus.PAUSED,
            actual = bibleVersionDao.versions.getValue(VERSION_ID).status,
        )
        assertTrue("dismiss $VERSION_ID" in notifier.calls)
    }

    @Test
    fun `GIVEN a running download WHEN deleting it THEN cancels it and resets the version`() = runTest {
        // Given
        facade.downloadVersion(VERSION_ID)

        // When
        facade.deleteDownload(VERSION_ID)

        // Then
        assertFalse(downloader.hasActiveDownload(VERSION_ID))
        assertEquals(
            expected = DownloadStatus.NOT_STARTED,
            actual = bibleVersionDao.versions.getValue(VERSION_ID).status,
        )
        assertTrue("dismiss $VERSION_ID" in notifier.calls)
    }

    private companion object {
        const val VERSION_ID = "acf"
    }
}
