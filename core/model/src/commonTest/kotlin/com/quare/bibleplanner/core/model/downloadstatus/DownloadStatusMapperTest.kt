package com.quare.bibleplanner.core.model.downloadstatus

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DownloadStatusMapperTest {
    private val mapper = DownloadStatusMapper()

    @Test
    fun `GIVEN each stored status WHEN mapping it THEN picks the matching download state`() {
        // Given
        val storedStatuses = listOf(
            DownloadStatus.NOT_STARTED to 0f,
            DownloadStatus.IN_PROGRESS to 0.4f,
            DownloadStatus.PAUSED to 0.6f,
            DownloadStatus.DONE to 0.2f,
        )

        // When
        val states = storedStatuses.map { (status, progress) ->
            mapper.map(
                status = status,
                progress = progress,
            )
        }

        // Then
        assertEquals(
            listOf(
                DownloadStatusModel.NotStarted,
                DownloadStatusModel.InProgress.Downloading(0.4f),
                DownloadStatusModel.InProgress.Paused(0.6f),
                DownloadStatusModel.Downloaded,
            ),
            states,
        )
    }

    @Test
    fun `GIVEN an in progress download that reached every chapter WHEN mapping it THEN reports it downloaded`() {
        // Given
        val status = DownloadStatus.IN_PROGRESS
        val progress = 1f

        // When
        val state = mapper.map(
            status = status,
            progress = progress,
        )

        // Then
        assertEquals(DownloadStatusModel.Downloaded, state)
    }

    @Test
    fun `GIVEN progress values WHEN formatting them THEN shows a percentage with at most two decimals`() {
        // Given
        val progressValues = listOf(0f, 0.9f, 0.9025f, 0.12345f, 0.0105f)

        // When
        val labels = progressValues.map(::formatDownloadProgress)

        // Then
        assertEquals(listOf("0", "90", "90.25", "12.35", "1.05"), labels)
    }

    @Test
    fun `GIVEN an in progress download WHEN reading its label THEN formats its progress`() {
        // Given
        val download = DownloadStatusModel.InProgress.Paused(0.5f)

        // When
        val label = download.progressStr

        // Then
        assertEquals("50", label)
    }
}
