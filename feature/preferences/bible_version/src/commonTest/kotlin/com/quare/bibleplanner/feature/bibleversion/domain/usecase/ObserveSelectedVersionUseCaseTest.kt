package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.books.testing.FakeBibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.provider.room.testing.FakeBibleVersionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ObserveSelectedVersionUseCaseTest {
    private lateinit var downloaderFacade: FakeBibleVersionDownloaderFacade
    private lateinit var useCase: ObserveSelectedVersionUseCase

    @Test
    fun `GIVEN an interrupted download of the selected version WHEN observing THEN resumes it`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.IN_PROGRESS)

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertEquals(listOf("download $SELECTED_VERSION_ID"), downloaderFacade.calls)
    }

    @Test
    fun `GIVEN a selected version never downloaded WHEN observing THEN does not download it`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.NOT_STARTED)

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertTrue(downloaderFacade.calls.isEmpty())
    }

    @Test
    fun `GIVEN a paused selected version WHEN observing THEN does not download it`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.PAUSED)

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertTrue(downloaderFacade.calls.isEmpty())
    }

    @Test
    fun `GIVEN a downloaded selected version WHEN observing THEN does not download it`() = runTest {
        // Given
        prepareScenario(status = DownloadStatus.DONE)

        // When
        backgroundScope.launch { useCase() }
        runCurrent()

        // Then
        assertTrue(downloaderFacade.calls.isEmpty())
    }

    private fun prepareScenario(status: DownloadStatus) {
        downloaderFacade = FakeBibleVersionDownloaderFacade(shouldShowDownloadTip = false)
        useCase = ObserveSelectedVersionUseCase(
            bibleVersionDao = FakeBibleVersionDao(
                versions = listOf(
                    BibleVersionEntity(
                        id = SELECTED_VERSION_ID,
                        status = status,
                        totalChapters = 1189,
                        contentVersion = "1.3.0",
                    ),
                ),
            ),
            bibleVersionDownloaderFacade = downloaderFacade,
            getSelectedVersionAbbreviationFlow = GetSelectedVersionIdFlowUseCase(
                FakeBibleRepository(
                    bibles = emptyList(),
                    selectedVersionId = SELECTED_VERSION_ID,
                ),
            ),
        )
    }

    private companion object {
        const val SELECTED_VERSION_ID = "ACF"
    }
}
