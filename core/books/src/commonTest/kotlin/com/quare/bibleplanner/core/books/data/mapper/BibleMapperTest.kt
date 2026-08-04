package com.quare.bibleplanner.core.books.data.mapper

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
    fun `flags a pending update when the downloaded content version is outdated`() {
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
    fun `flags a pending update when a done version misses chapters`() {
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
    fun `does not flag a complete up-to-date version`() {
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
    fun `does not flag a version that was never downloaded`() {
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
    fun `does not flag an outdated version while it is downloading`() {
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
    fun `does not flag an outdated version when the remote content version is blank`() {
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
    ) = mapper
        .map(
            dataBaseVersions = listOf(entity),
            supportedVersions = listOf(
                VersionModel(
                    id = "ACF",
                    name = "Almeida Corrigida Fiel",
                    version = remoteContentVersion,
                    language = Language.PORTUGUESE_BRAZIL,
                    chapters = 1189,
                ),
            ),
            downloadedChaptersMap = mapOf("ACF" to downloadedChapters),
        ).single()
}
