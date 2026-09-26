package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.fake.FakeBibleRepository
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SelectedBibleUseCasesTest {
    private lateinit var repository: FakeBibleRepository

    @Test
    fun `GIVEN a stored selection WHEN observing the selected version id THEN emits it`() = runTest {
        // Given
        prepareScenario(bibles = emptyList())

        // When
        val versionId = GetSelectedVersionIdFlowUseCase(repository)().first()

        // Then
        assertEquals("WEB", versionId)
    }

    @Test
    fun `GIVEN a selected bible WHEN observing the selected bible and its name THEN emits that bible`() = runTest {
        // Given
        val selected = bible(
            id = "KJV",
            isSelected = true,
            downloadStatus = DownloadStatusModel.Downloaded,
        )
        prepareScenario(
            bibles = listOf(
                bible(
                    id = "WEB",
                    isSelected = false,
                    downloadStatus = DownloadStatusModel.Downloaded,
                ),
                selected,
            ),
        )
        val getSelectedBible = GetSelectedBibleFlowUseCase(repository)

        // When
        val bible = getSelectedBible().first()
        val name = GetSelectedBibleNameFlowUseCase(getSelectedBible)().first()

        // Then
        assertEquals(selected, bible)
        assertEquals("KJV name", name)
    }

    @Test
    fun `GIVEN no selected bible WHEN observing the selected bible name THEN emits an empty name`() = runTest {
        // Given
        prepareScenario(bibles = emptyList())
        val getSelectedBible = GetSelectedBibleFlowUseCase(repository)

        // When
        val bible = getSelectedBible().first()
        val name = GetSelectedBibleNameFlowUseCase(getSelectedBible)().first()

        // Then
        assertNull(bible)
        assertEquals("", name)
    }

    @Test
    fun `GIVEN a download in progress WHEN observing its progress THEN emits each distinct progress until complete`() =
        runTest {
            // Given
            prepareScenario(bibles = listOf(downloading(0.25f)))
            val progress = mutableListOf<Float>()
            backgroundScope.launch {
                ObserveBibleVersionDownloadProgressUseCase(repository)("WEB").take(3).toList(progress)
            }
            runCurrent()

            // When
            repository.bibles.value = listOf(downloading(0.25f))
            runCurrent()
            repository.bibles.value = listOf(
                bible(
                    id = "WEB",
                    isSelected = true,
                    downloadStatus = DownloadStatusModel.InProgress.Paused(0.5f),
                ),
            )
            runCurrent()
            repository.bibles.value = listOf(
                bible(
                    id = "WEB",
                    isSelected = true,
                    downloadStatus = DownloadStatusModel.Downloaded,
                ),
            )
            runCurrent()

            // Then
            assertEquals(listOf(0.25f, 0.5f, 1f), progress)
        }

    @Test
    fun `GIVEN an unknown version WHEN observing its progress THEN emits nothing`() = runTest {
        // Given
        prepareScenario(bibles = listOf(downloading(0.25f)))
        val progress = mutableListOf<Float>()
        backgroundScope.launch {
            ObserveBibleVersionDownloadProgressUseCase(repository)("ACF").toList(progress)
        }

        // When
        runCurrent()

        // Then
        assertEquals(emptyList(), progress)
    }

    private fun downloading(progress: Float): BibleModel = bible(
        id = "WEB",
        isSelected = true,
        downloadStatus = DownloadStatusModel.InProgress.Downloading(progress),
    )

    private fun bible(
        id: String,
        isSelected: Boolean,
        downloadStatus: DownloadStatusModel,
    ): BibleModel = BibleModel(
        version = VersionModel(
            id = id,
            name = "$id name",
            version = "1.0.0",
            language = Language.ENGLISH,
            chapters = 1189,
            size = null,
        ),
        downloadedChapters = 0,
        downloadStatus = downloadStatus,
        isSelected = isSelected,
        hasPendingUpdate = false,
    )

    private fun prepareScenario(bibles: List<BibleModel>) {
        repository = FakeBibleRepository(
            bibles = bibles,
            selectedVersionId = "WEB",
        )
    }
}
