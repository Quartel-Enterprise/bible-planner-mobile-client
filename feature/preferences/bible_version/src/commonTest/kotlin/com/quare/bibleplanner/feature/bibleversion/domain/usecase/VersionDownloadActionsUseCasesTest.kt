package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryBibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.fake.RecordingDownloadNotifier
import com.quare.bibleplanner.feature.bibleversion.fake.ThrowingVerseDao
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class VersionDownloadActionsUseCasesTest {
    private lateinit var bibleVersionDao: InMemoryBibleVersionDao
    private lateinit var notifier: RecordingDownloadNotifier
    private lateinit var downloaderFacade: FakeBibleVersionDownloaderFacade
    private lateinit var deletedVerseTexts: MutableList<String>

    @BeforeTest
    fun setUp() {
        bibleVersionDao = InMemoryBibleVersionDao(
            listOf(
                BibleVersionEntity(
                    id = VERSION_ID,
                    status = DownloadStatus.IN_PROGRESS,
                ),
            ),
        )
        notifier = RecordingDownloadNotifier()
        downloaderFacade = FakeBibleVersionDownloaderFacade()
        deletedVerseTexts = mutableListOf()
    }

    @Test
    fun `pausing dismisses the notification and marks the version paused`() = runTest {
        // Given
        val useCase = PauseBibleVersionDownloadUseCase(
            bibleVersionDao = bibleVersionDao,
            notifier = notifier,
        )

        // When
        useCase(VERSION_ID)

        // Then
        assertEquals(
            expected = listOf("dismiss $VERSION_ID"),
            actual = notifier.calls,
        )
        assertEquals(
            expected = DownloadStatus.PAUSED,
            actual = bibleVersionDao.versions[VERSION_ID]?.status,
        )
    }

    @Test
    fun `updating wipes the downloaded texts before downloading the version again`() = runTest {
        // Given
        val useCase = UpdateBibleVersionUseCase(
            deleteBibleVersionDownload = DeleteBibleVersionDownloadUseCase(
                bibleVersionDao = bibleVersionDao,
                verseDao = RecordingDeleteVerseDao(deletedVerseTexts),
                notifier = notifier,
            ),
            downloaderFacade = downloaderFacade,
        )

        // When
        useCase(VERSION_ID)

        // Then
        assertEquals(
            expected = listOf(VERSION_ID),
            actual = deletedVerseTexts,
        )
        assertEquals(
            expected = DownloadStatus.NOT_STARTED,
            actual = bibleVersionDao.versions[VERSION_ID]?.status,
        )
        assertEquals(
            expected = listOf("download $VERSION_ID"),
            actual = downloaderFacade.calls,
        )
    }

    @Test
    fun `selecting a version stores it as the selected one`() = runTest {
        // Given
        val repository = FakeBibleRepository()
        val useCase = SetSelectedVersionUseCase(repository)

        // When
        useCase(VERSION_ID)

        // Then
        assertEquals(
            expected = listOf(VERSION_ID),
            actual = repository.selectedVersionIds,
        )
    }

    private companion object {
        const val VERSION_ID = "acf"
    }
}

private class RecordingDeleteVerseDao(
    private val deletedVersions: MutableList<String>,
) : ThrowingVerseDao() {
    override suspend fun deleteVerseTextsByVersion(versionId: String) {
        deletedVersions += versionId
    }
}
