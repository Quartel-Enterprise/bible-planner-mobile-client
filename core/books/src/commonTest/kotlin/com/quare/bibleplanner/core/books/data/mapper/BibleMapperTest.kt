package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.utils.locale.Language
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BibleMapperTest {
    private lateinit var mapper: BibleMapper

    @BeforeTest
    fun setUp() {
        mapper = BibleMapper(DownloadStatusMapper())
    }

    @Test
    fun `GIVEN a downloaded outdated content version WHEN mapping THEN flags a pending update`() {
        // Given
        val entity = entity(
            status = DownloadStatus.DONE,
            contentVersion = "1.1.0",
        )

        // When
        val bible = map(
            entity = entity,
            downloadedChapters = 1189,
        )

        // Then
        assertTrue(bible.hasPendingUpdate)
    }

    @Test
    fun `GIVEN a done version missing chapters WHEN mapping THEN flags a pending update`() {
        // Given
        val entity = entity(
            status = DownloadStatus.DONE,
            contentVersion = "1.2.0",
        )

        // When
        val bible = map(
            entity = entity,
            downloadedChapters = 1180,
        )

        // Then
        assertTrue(bible.hasPendingUpdate)
    }

    @Test
    fun `GIVEN a complete up-to-date version WHEN mapping THEN does not flag a pending update`() {
        // Given
        val entity = entity(
            status = DownloadStatus.DONE,
            contentVersion = "1.2.0",
        )

        // When
        val bible = map(
            entity = entity,
            downloadedChapters = 1189,
        )

        // Then
        assertFalse(bible.hasPendingUpdate)
    }

    @Test
    fun `GIVEN a version never downloaded WHEN mapping THEN does not flag a pending update`() {
        // Given
        val entity = entity(
            status = DownloadStatus.NOT_STARTED,
            contentVersion = "1.1.0",
        )

        // When
        val bible = map(
            entity = entity,
            downloadedChapters = 0,
        )

        // Then
        assertFalse(bible.hasPendingUpdate)
    }

    @Test
    fun `GIVEN an outdated version being downloaded WHEN mapping THEN does not flag a pending update`() {
        // Given
        val entity = entity(
            status = DownloadStatus.IN_PROGRESS,
            contentVersion = "1.1.0",
        )

        // When
        val bible = map(
            entity = entity,
            downloadedChapters = 500,
        )

        // Then
        assertFalse(bible.hasPendingUpdate)
    }

    @Test
    fun `GIVEN a blank remote content version WHEN mapping an outdated version THEN does not flag an update`() {
        // Given
        val entity = entity(
            status = DownloadStatus.DONE,
            contentVersion = "1.1.0",
        )

        // When
        val bible = map(
            entity = entity,
            downloadedChapters = 1189,
            remoteContentVersion = "",
        )

        // Then
        assertFalse(bible.hasPendingUpdate)
    }

    private fun entity(
        status: DownloadStatus,
        contentVersion: String,
    ): BibleVersionEntity = BibleVersionEntity(
        id = "ACF",
        status = status,
        totalChapters = 1189,
        contentVersion = contentVersion,
    )

    private fun map(
        entity: BibleVersionEntity,
        downloadedChapters: Int,
        remoteContentVersion: String = "1.2.0",
    ): BibleModel = mapper
        .map(
            dataBaseVersions = listOf(entity),
            supportedVersions = listOf(
                VersionModel(
                    id = "ACF",
                    name = "Almeida Corrigida Fiel",
                    version = remoteContentVersion,
                    language = Language.PORTUGUESE_BRAZIL,
                    chapters = 1189,
                    size = 8245560,
                ),
            ),
            downloadedChaptersMap = mapOf("ACF" to downloadedChapters),
        ).single()
}
