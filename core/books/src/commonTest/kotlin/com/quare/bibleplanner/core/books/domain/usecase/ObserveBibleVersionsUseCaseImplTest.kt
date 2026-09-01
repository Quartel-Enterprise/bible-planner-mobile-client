package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.books.fake.RecordingBibleVersionDao
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveBibleVersionsUseCaseImplTest {
    private lateinit var useCase: ObserveBibleVersionsUseCaseImpl
    private lateinit var bibleVersionDao: RecordingBibleVersionDao

    @Test
    fun `inserts a version published after the first emission`() = runTest {
        // Given
        prepareScenario(
            emissions = listOf(
                listOf(versionModel(id = "ACF")),
                listOf(
                    versionModel(id = "ACF"),
                    versionModel(id = "KJV"),
                ),
            ),
        )

        // When
        useCase()

        // Then
        assertEquals(
            expected = listOf("ACF", "KJV"),
            actual = bibleVersionDao.insertedVersions.map { it.id },
        )
    }

    @Test
    fun `does not insert a version twice when the list is emitted again`() = runTest {
        // Given
        prepareScenario(
            emissions = listOf(
                listOf(versionModel(id = "ACF")),
                listOf(versionModel(id = "ACF")),
            ),
        )

        // When
        useCase()

        // Then
        assertEquals(
            expected = listOf("ACF"),
            actual = bibleVersionDao.insertedVersions.map { it.id },
        )
    }

    private fun versionModel(id: String): VersionModel = VersionModel(
        id = id,
        name = "Almeida Corrigida Fiel",
        version = "1.2.0",
        language = Language.PORTUGUESE_BRAZIL,
        chapters = 1189,
        size = 8245560,
    )

    private fun prepareScenario(emissions: List<List<VersionModel>>) {
        bibleVersionDao = RecordingBibleVersionDao(emptyList())
        useCase = ObserveBibleVersionsUseCaseImpl(
            metadataRepository = FakeObservingBibleVersionRepository(flowOf(*emissions.toTypedArray())),
            syncBibleVersions = SyncBibleVersionsUseCase(
                bibleVersionDao = bibleVersionDao,
                verseDao = ThrowingVerseDao(),
            ),
        )
    }
}

private class FakeObservingBibleVersionRepository(
    private val versions: Flow<List<VersionModel>>,
) : BibleVersionRepository {
    override suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>> = error("Unexpected call")

    override fun observeVersions(): Flow<List<VersionModel>> = versions
}
