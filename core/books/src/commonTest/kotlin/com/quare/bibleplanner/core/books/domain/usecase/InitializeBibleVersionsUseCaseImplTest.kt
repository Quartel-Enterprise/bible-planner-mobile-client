package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.books.fake.RecordingBibleVersionDao
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InitializeBibleVersionsUseCaseImplTest {
    private lateinit var useCase: InitializeBibleVersionsUseCaseImpl
    private lateinit var bibleVersionDao: RecordingBibleVersionDao

    @Test
    fun `syncs every fetched version`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.success(
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
    fun `does nothing when fetching the remote versions fails`() = runTest {
        // Given
        prepareScenario(
            remoteVersions = Result.failure(IllegalStateException("offline")),
        )

        // When
        useCase()

        // Then
        assertTrue(bibleVersionDao.insertedVersions.isEmpty())
    }

    private fun versionModel(id: String): VersionModel = VersionModel(
        id = id,
        name = "Almeida Corrigida Fiel",
        version = "1.2.0",
        language = Language.PORTUGUESE_BRAZIL,
        chapters = 1189,
        size = 8245560,
    )

    private fun prepareScenario(remoteVersions: Result<List<VersionModel>>) {
        bibleVersionDao = RecordingBibleVersionDao(emptyList())
        useCase = InitializeBibleVersionsUseCaseImpl(
            metadataRepository = FakeBibleVersionRepository(remoteVersions),
            syncBibleVersions = SyncBibleVersionsUseCase(
                bibleVersionDao = bibleVersionDao,
                verseDao = ThrowingVerseDao(),
            ),
        )
    }
}

private class FakeBibleVersionRepository(
    private val remoteVersions: Result<List<VersionModel>>,
) : BibleVersionRepository {
    override suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>> = remoteVersions

    override fun observeVersions(): Flow<List<VersionModel>> = error("Unexpected call")
}
