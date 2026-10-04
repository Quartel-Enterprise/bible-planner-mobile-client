package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVersionModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetListeningVersionUseCaseTest {
    private lateinit var useCase: GetListeningVersionUseCase

    @Test
    fun `GIVEN a Portuguese Bible selected WHEN getting the version THEN reads it with a Brazilian voice`() = runTest {
        // Given
        prepareScenario(
            bible("acf", Language.PORTUGUESE_BRAZIL, isSelected = true),
            bible("kjv", Language.ENGLISH, isSelected = false),
        )

        // When
        val version = useCase()

        // Then
        assertEquals(ListeningVersionModel(id = "acf", languageTag = "pt-BR"), version)
    }

    @Test
    fun `GIVEN an English Bible selected WHEN getting the version THEN reads it with an American voice`() = runTest {
        // Given
        prepareScenario(bible("kjv", Language.ENGLISH, isSelected = true))

        // When
        val version = useCase()

        // Then
        assertEquals("en-US", version?.languageTag)
    }

    @Test
    fun `GIVEN a Spanish Bible selected WHEN getting the version THEN reads it with a Latin American voice`() =
        runTest {
            // Given
            prepareScenario(bible("rvr", Language.SPANISH, isSelected = true))

            // When
            val version = useCase()

            // Then
            assertEquals("es-MX", version?.languageTag)
        }

    @Test
    fun `GIVEN no Bible selected WHEN getting the version THEN has none`() = runTest {
        // Given
        prepareScenario(bible("kjv", Language.ENGLISH, isSelected = false))

        // When
        val version = useCase()

        // Then
        assertNull(version)
    }

    private fun bible(
        id: String,
        language: Language,
        isSelected: Boolean,
    ): BibleModel = BibleModel(
        version = VersionModel(
            id = id,
            name = id.uppercase(),
            version = "1",
            language = language,
            chapters = 1189,
            size = null,
        ),
        downloadedChapters = 1189,
        downloadStatus = DownloadStatusModel.NotStarted,
        isSelected = isSelected,
        hasPendingUpdate = false,
    )

    private fun prepareScenario(vararg bibles: BibleModel) {
        useCase = GetListeningVersionUseCase(
            FakeBibleRepository(
                bibles = bibles.toList(),
                selectedVersionId = bibles.first().version.id,
            ),
        )
    }
}
