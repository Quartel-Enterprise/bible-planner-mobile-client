package com.quare.bibleplanner.core.books.domain.usecase

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

    @Test
    fun `inserts an unknown version with its remote content version`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(version = "1.2.0"))),
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
    fun `keeps an up-to-date version untouched`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(version = "1.2.0"))),
            existingVersions = listOf(versionEntity(contentVersion = "1.2.0")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase()

        // Then
        assertTrue(bibleVersionDao.insertedVersions.isEmpty())
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
    }

    @Test
    fun `adopts the remote content version when nothing was downloaded`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(version = "1.2.0"))),
            existingVersions = listOf(versionEntity(contentVersion = "1.1.0")),
            downloadedChaptersCount = 0,
        )

        // When
        useCase()

        // Then
        assertEquals("1.2.0", bibleVersionDao.updatedVersions.single().contentVersion)
    }

    @Test
    fun `keeps the stored content version when outdated content is downloaded`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(version = "1.2.0"))),
            existingVersions = listOf(versionEntity(contentVersion = "1.1.0")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase()

        // Then
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
    }

    @Test
    fun `keeps the stored content version when the remote content version is blank`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(listOf(versionModel(version = ""))),
            existingVersions = listOf(versionEntity(contentVersion = "1.1.0")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase()

        // Then
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
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
    }

    private fun versionModel(version: String): VersionModel = VersionModel(
        id = "ACF",
        name = "Almeida Corrigida Fiel",
        version = version,
        language = Language.PORTUGUESE_BRAZIL,
        chapters = 1189,
        size = 8245560,
    )

    private fun versionEntity(contentVersion: String): BibleVersionEntity = BibleVersionEntity(
        id = "ACF",
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
        useCase = InitializeBibleVersionsUseCaseImpl(
            bibleVersionDao = bibleVersionDao,
            verseDao = CountingVerseDao(downloadedChaptersCount),
            metadataRepository = { remoteVersions },
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
