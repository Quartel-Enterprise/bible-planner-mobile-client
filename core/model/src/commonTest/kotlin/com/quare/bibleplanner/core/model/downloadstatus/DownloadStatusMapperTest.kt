package com.quare.bibleplanner.core.model.downloadstatus

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DownloadStatusMapperTest {
    private val mapper = DownloadStatusMapper()

    @Test
    fun `GIVEN each stored status WHEN mapping it THEN picks the matching download state`() {
        // When
        val states = listOf(
            mapper.map(
                status = DownloadStatus.NOT_STARTED,
                progress = 0f,
            ),
            mapper.map(
                status = DownloadStatus.IN_PROGRESS,
                progress = 0.4f,
            ),
            mapper.map(
                status = DownloadStatus.PAUSED,
                progress = 0.6f,
            ),
            mapper.map(
                status = DownloadStatus.DONE,
                progress = 0.2f,
            ),
        )

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
        // When
        val state = mapper.map(
            status = DownloadStatus.IN_PROGRESS,
            progress = 1f,
        )

        // Then
        assertEquals(DownloadStatusModel.Downloaded, state)
    }

    @Test
    fun `GIVEN progress values WHEN formatting them THEN shows a percentage with at most two decimals`() {
        // When
        val labels = listOf(0f, 0.9f, 0.9025f, 0.12345f, 0.0105f).map(::formatDownloadProgress)

        // Then
        assertEquals(listOf("0", "90", "90.25", "12.35", "1.05"), labels)
    }

    @Test
    fun `GIVEN an in progress download WHEN reading its label THEN formats its progress`() {
        // When
        val label = DownloadStatusModel.InProgress.Paused(0.5f).progressStr

        // Then
        assertEquals("50", label)
    }
}
