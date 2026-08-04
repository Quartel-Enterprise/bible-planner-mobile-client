package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InitializeBibleVersionsUseCaseImplTest {
    private lateinit var useCase: InitializeBibleVersionsUseCaseImpl
    private lateinit var bibleVersionDao: RecordingBibleVersionDao
    private lateinit var downloaderFacade: RecordingDownloaderFacade

    @Test
    fun `inserts an unknown version with its remote content version`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(id = "ACF", version = "1.2.0"))),
        )

        // When
        useCase()

        // Then
        val inserted = bibleVersionDao.insertedVersions.single()
        assertEquals("ACF", inserted.id)
        assertEquals(DownloadStatus.NOT_STARTED, inserted.status)
        assertEquals(1189, inserted.totalChapters)
        assertEquals("1.2.0", inserted.contentVersion)
    }

    @Test
    fun `keeps a complete up-to-date version untouched`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(id = "ACF", version = "1.2.0"))),
            existingVersions = listOf(versionEntity(id = "ACF", contentVersion = "1.2.0")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase()

        // Then
        assertTrue(bibleVersionDao.insertedVersions.isEmpty())
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
        assertTrue(downloaderFacade.downloadedVersionIds.isEmpty())
    }

    @Test
    fun `adopts the remote content version without downloading when nothing was downloaded`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(id = "ACF", version = "1.2.0"))),
            existingVersions = listOf(versionEntity(id = "ACF", contentVersion = "1.1.0")),
            downloadedChaptersCount = 0,
        )

        // When
        useCase()

        // Then
        assertEquals("1.2.0", bibleVersionDao.updatedVersions.single().contentVersion)
        assertTrue(downloaderFacade.downloadedVersionIds.isEmpty())
    }

    @Test
    fun `schedules a refresh download without touching the stored content version when content is outdated`() =
        runTest {
            // Given
            prepareScenario(
                remoteVersions = Result.success(listOf(versionModel(id = "ACF", version = "1.2.0"))),
                existingVersions = listOf(versionEntity(id = "ACF", contentVersion = "1.1.0")),
                downloadedChaptersCount = 1189,
            )

            // When
            useCase()

            // Then
            assertTrue(bibleVersionDao.updatedVersions.isEmpty())
            assertEquals(listOf("ACF"), downloaderFacade.downloadedVersionIds)
        }

    @Test
    fun `schedules a refresh for a version stored before content versions were tracked`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(id = "ACF", version = "1.2.0"))),
            existingVersions = listOf(versionEntity(id = "ACF", contentVersion = "")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase()

        // Then
        assertEquals(listOf("ACF"), downloaderFacade.downloadedVersionIds)
    }

    @Test
    fun `schedules a healing download when a done version misses chapters`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(id = "ACF", version = "1.2.0"))),
            existingVersions = listOf(versionEntity(id = "ACF", contentVersion = "1.2.0")),
            downloadedChaptersCount = 1180,
        )

        // When
        useCase()

        // Then
        assertEquals(listOf("ACF"), downloaderFacade.downloadedVersionIds)
    }

    @Test
    fun `schedules at most one download per run when several versions are outdated`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(
                listOf(
                    versionModel(id = "ACF", version = "1.2.0"),
                    versionModel(id = "WEB", version = "1.1.0"),
                ),
            ),
            existingVersions = listOf(
                versionEntity(id = "ACF", contentVersion = ""),
                versionEntity(id = "WEB", contentVersion = ""),
            ),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase()

        // Then
        assertEquals(listOf("ACF"), downloaderFacade.downloadedVersionIds)
    }

    @Test
    fun `does not schedule a refresh when the remote content version is blank`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(id = "ACF", version = ""))),
            existingVersions = listOf(versionEntity(id = "ACF", contentVersion = "1.1.0")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase()

        // Then
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
        assertTrue(downloaderFacade.downloadedVersionIds.isEmpty())
    }

    @Test
    fun `does nothing when fetching the remote versions fails`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.failure(IllegalStateException("offline")),
        )

        // When
        useCase()

        // Then
        assertTrue(bibleVersionDao.insertedVersions.isEmpty())
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
        assertTrue(downloaderFacade.downloadedVersionIds.isEmpty())
    }

    private fun versionModel(
        id: String,
        version: String,
    ): VersionModel = VersionModel(
        id = id,
        name = "Version $id",
        version = version,
        language = Language.PORTUGUESE_BRAZIL,
        chapters = 1189,
    )

    private fun versionEntity(
        id: String,
        contentVersion: String,
    ): BibleVersionEntity = BibleVersionEntity(
        id = id,
        status = DownloadStatus.DONE,
        totalChapters = 1189,
        contentVersion = contentVersion,
    )

    private fun prepareScenario(
        remoteVersions: Result<List<VersionModel>>,
        existingVersions: List<BibleVersionEntity> = emptyList(),
        downloadedChaptersCount: Int = 0,
    ) {
        bibleVersionDao = RecordingBibleVersionDao(existingVersions)
        downloaderFacade = RecordingDownloaderFacade()
        useCase = InitializeBibleVersionsUseCaseImpl(
            bibleVersionDao = bibleVersionDao,
            verseDao = CountingVerseDao(downloadedChaptersCount),
            metadataRepository = { remoteVersions },
            downloaderFacade = downloaderFacade,
        )
    }
}

private class RecordingBibleVersionDao(
    private val existingVersions: List<BibleVersionEntity>,
) : BibleVersionDao {
    val insertedVersions = mutableListOf<BibleVersionEntity>()
    val updatedVersions = mutableListOf<BibleVersionEntity>()

    override suspend fun getVersionById(id: String): BibleVersionEntity? = existingVersions.find { it.id == id }

    override suspend fun insertVersion(version: BibleVersionEntity) {
        insertedVersions += version
    }

    override suspend fun updateVersion(version: BibleVersionEntity) {
        updatedVersions += version
    }

    override fun getAllVersionsFlow(): Flow<List<BibleVersionEntity>> = emptyFlow()

    override suspend fun getAllVersions(): List<BibleVersionEntity> = error("Unexpected call")

    override suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
    ) = error("Unexpected call")

    override suspend fun updateContentVersion(
        id: String,
        contentVersion: String,
    ) = error("Unexpected call")

    override suspend fun deleteVersion(id: String) = error("Unexpected call")
}

private class CountingVerseDao(
    private val downloadedChaptersCount: Int,
) : ThrowingVerseDao() {
    override suspend fun countChaptersWithVersesByVersion(versionId: String): Int = downloadedChaptersCount
}

private class RecordingDownloaderFacade : BibleVersionDownloaderFacade {
    val downloadedVersionIds = mutableListOf<String>()

    override val shouldShowDownloadTip: Boolean = false

    override fun downloadVersion(versionId: String) {
        downloadedVersionIds += versionId
    }

    override suspend fun pauseDownload(versionId: String) = error("Unexpected call")

    override suspend fun deleteDownload(versionId: String) = error("Unexpected call")
}
