package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.fake.RecordingBibleVersionDao
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SyncBibleVersionsUseCaseTest {
    private lateinit var useCase: SyncBibleVersionsUseCase
    private lateinit var bibleVersionDao: RecordingBibleVersionDao

    @Test
    fun `inserts an unknown version with its remote content version`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase(listOf(versionModel(version = "1.2.0")))

        // Then
        val inserted = bibleVersionDao.insertedVersions.single()
        assertEquals("ACF", inserted.id)
        assertEquals(DownloadStatus.NOT_STARTED, inserted.status)
        assertEquals(1189, inserted.totalChapters)
        assertEquals("1.2.0", inserted.contentVersion)
    }

    @Test
    fun `inserts only the versions that are missing locally`() = runTest {
        // Given
        prepareScenario(
            existingVersions = listOf(versionEntity(contentVersion = "1.2.0")),
        )

        // When
        useCase(
            listOf(
                versionModel(version = "1.2.0"),
                versionModel(
                    id = "KJV",
                    version = "1.0.0",
                ),
            ),
        )

        // Then
        assertEquals("KJV", bibleVersionDao.insertedVersions.single().id)
    }

    @Test
    fun `keeps an up-to-date version untouched`() = runTest {
        // Given
        prepareScenario(
            existingVersions = listOf(versionEntity(contentVersion = "1.2.0")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase(listOf(versionModel(version = "1.2.0")))

        // Then
        assertTrue(bibleVersionDao.insertedVersions.isEmpty())
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
    }

    @Test
    fun `adopts the remote content version when nothing was downloaded`() = runTest {
        // Given
        prepareScenario(
            existingVersions = listOf(versionEntity(contentVersion = "1.1.0")),
            downloadedChaptersCount = 0,
        )

        // When
        useCase(listOf(versionModel(version = "1.2.0")))

        // Then
        assertEquals("1.2.0", bibleVersionDao.updatedVersions.single().contentVersion)
    }

    @Test
    fun `keeps the stored content version when outdated content is downloaded`() = runTest {
        // Given
        prepareScenario(
            existingVersions = listOf(versionEntity(contentVersion = "1.1.0")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase(listOf(versionModel(version = "1.2.0")))

        // Then
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
    }

    @Test
    fun `keeps the stored content version when the remote content version is blank`() = runTest {
        // Given
        prepareScenario(
            existingVersions = listOf(versionEntity(contentVersion = "1.1.0")),
            downloadedChaptersCount = 1189,
        )

        // When
        useCase(listOf(versionModel(version = "")))

        // Then
        assertTrue(bibleVersionDao.updatedVersions.isEmpty())
    }

    private fun versionModel(
        version: String,
        id: String = "ACF",
    ): VersionModel = VersionModel(
        id = id,
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
        existingVersions: List<BibleVersionEntity> = emptyList(),
        downloadedChaptersCount: Int = 0,
    ) {
        bibleVersionDao = RecordingBibleVersionDao(existingVersions)
        useCase = SyncBibleVersionsUseCase(
            bibleVersionDao = bibleVersionDao,
            verseDao = CountingVerseDao(downloadedChaptersCount),
        )
    }
}

private class CountingVerseDao(
    private val downloadedChaptersCount: Int,
) : ThrowingVerseDao() {
    override suspend fun countChaptersWithVersesByVersion(versionId: String): Int = downloadedChaptersCount
}
