package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.dao.BibleVersionDao
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val SELECTED_VERSION_ID = "ACF"

internal class ObserveSelectedVersionUseCaseTest {
    private lateinit var downloaderFacade: FakeBibleVersionDownloaderFacade
    private lateinit var useCase: ObserveSelectedVersionUseCase

    @Test
    fun `resumes the selected version when its download was interrupted`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.IN_PROGRESS)

        // When
        useCase()

        // Then
        assertEquals(listOf(SELECTED_VERSION_ID), downloaderFacade.downloadedVersionIds)
    }

    @Test
    fun `does not download the selected version that was never downloaded`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.NOT_STARTED)

        // When
        useCase()

        // Then
        assertTrue(downloaderFacade.downloadedVersionIds.isEmpty())
    }

    @Test
    fun `does not download the selected version that is paused`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.PAUSED)

        // When
        useCase()

        // Then
        assertTrue(downloaderFacade.downloadedVersionIds.isEmpty())
    }

    @Test
    fun `does not download the selected version that is already downloaded`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.DONE)

        // When
        useCase()

        // Then
        assertTrue(downloaderFacade.downloadedVersionIds.isEmpty())
    }

    private fun prepareScenario(status: DownloadStatus) {
        downloaderFacade = FakeBibleVersionDownloaderFacade()
        useCase = ObserveSelectedVersionUseCase(
            bibleVersionDao = FakeBibleVersionDao(status),
            bibleVersionDownloaderFacade = downloaderFacade,
            getSelectedVersionAbbreviationFlow = GetSelectedVersionIdFlowUseCase(FakeSelectedVersionRepository()),
        )
    }
}

private class FakeBibleVersionDownloaderFacade : BibleVersionDownloaderFacade {
    val downloadedVersionIds = mutableListOf<String>()

    override val shouldShowDownloadTip: Boolean = false

    override fun downloadVersion(versionId: String) {
        downloadedVersionIds += versionId
    }

    override suspend fun pauseDownload(versionId: String) = Unit

    override suspend fun deleteDownload(versionId: String) = Unit
}

private class FakeSelectedVersionRepository : BibleRepository {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = flowOf(emptyList())

    override fun getSelectedVersionIdFlow(): Flow<String> = flowOf(SELECTED_VERSION_ID)

    override suspend fun setSelectedVersionId(id: String) = Unit
}

private class FakeBibleVersionDao(
    private val status: DownloadStatus,
) : BibleVersionDao {
    override fun getAllVersionsFlow(): Flow<List<BibleVersionEntity>> = flowOf(emptyList())

    override suspend fun getAllVersions(): List<BibleVersionEntity> = emptyList()

    override suspend fun getVersionById(id: String): BibleVersionEntity = BibleVersionEntity(
        id = id,
        status = status,
        totalChapters = 1189,
        contentVersion = "1.3.0",
    )

    override suspend fun insertVersion(version: BibleVersionEntity) = Unit

    override suspend fun updateVersion(version: BibleVersionEntity) = Unit

    override suspend fun updateStatus(
        id: String,
        status: DownloadStatus,
    ) = Unit

    override suspend fun updateContentVersion(
        id: String,
        contentVersion: String,
    ) = Unit

    override suspend fun deleteVersion(id: String) = Unit
}
