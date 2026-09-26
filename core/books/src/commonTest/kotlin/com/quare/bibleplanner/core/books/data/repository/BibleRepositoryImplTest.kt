package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.mapper.BibleMapper
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.books.fake.InMemoryPreferencesDataStore
import com.quare.bibleplanner.core.books.fake.ThrowingBibleVersionDao
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.provider.room.invalidation.TableInvalidationObserver
import com.quare.bibleplanner.core.provider.room.relation.VersionChapterCount
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BibleRepositoryImplTest {
    private val totalChapters = 1189
    private val contentVersion = "1.0.0"
    private val webVersion = VersionModel(
        id = "WEB",
        name = "World English Bible",
        version = contentVersion,
        language = Language.ENGLISH,
        chapters = totalChapters,
        size = null,
    )
    private val kjvVersion = VersionModel(
        id = "KJV",
        name = "King James Version",
        version = contentVersion,
        language = Language.ENGLISH,
        chapters = totalChapters,
        size = null,
    )

    private lateinit var repository: BibleRepositoryImpl

    @Test
    fun `defaults to the Portuguese version when the app is in Portuguese`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.PORTUGUESE_BRAZIL)

        // When
        val selectedVersionId = repository.getSelectedVersionIdFlow().first()

        // Then
        assertEquals(expected = "ACF", actual = selectedVersionId)
    }

    @Test
    fun `defaults to the Spanish version when the app is in Spanish`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.SPANISH)

        // When
        val selectedVersionId = repository.getSelectedVersionIdFlow().first()

        // Then
        assertEquals(expected = "RVR1960", actual = selectedVersionId)
    }

    @Test
    fun `defaults to the English version when the app is in English`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)

        // When
        val selectedVersionId = repository.getSelectedVersionIdFlow().first()

        // Then
        assertEquals(expected = "WEB", actual = selectedVersionId)
    }

    @Test
    fun `keeps the stored selection instead of the language default`() = runTest {
        // Given
        prepareScenario(
            appLanguage = Language.SPANISH,
            storedVersionId = "KJV",
        )

        // When
        val selectedVersionId = repository.getSelectedVersionIdFlow().first()

        // Then
        assertEquals(expected = "KJV", actual = selectedVersionId)
    }

    @Test
    fun `GIVEN a new selection WHEN storing it THEN the selected version flow emits it`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)

        // When
        repository.setSelectedVersionId("NVI")

        // Then
        assertEquals(expected = "NVI", actual = repository.getSelectedVersionIdFlow().first())
    }

    @Test
    fun `GIVEN supported and stored versions WHEN observing the bibles THEN marks the selected one ignoring case`() =
        runTest {
            // Given
            prepareScenario(
                appLanguage = Language.ENGLISH,
                storedVersionId = "kjv",
            )

            // When
            val bibles = repository.getBiblesFlow().first()

            // Then
            assertEquals(
                expected = listOf("WEB" to false, "KJV" to true),
                actual = bibles.map { it.version.id to it.isSelected },
            )
        }

    @Test
    fun `GIVEN downloaded chapter counts WHEN observing the bibles THEN reports each version progress`() = runTest {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)

        // When
        val bibles = repository.getBiblesFlow().first()

        // Then
        assertEquals(
            expected = listOf(
                bible(
                    version = webVersion,
                    downloadedChapters = totalChapters,
                    downloadStatus = DownloadStatusModel.Downloaded,
                    isSelected = true,
                ),
                bible(
                    version = kjvVersion,
                    downloadedChapters = 0,
                    downloadStatus = DownloadStatusModel.NotStarted,
                    isSelected = false,
                ),
            ),
            actual = bibles,
        )
    }

    private fun bible(
        version: VersionModel,
        downloadedChapters: Int,
        downloadStatus: DownloadStatusModel,
        isSelected: Boolean,
    ): BibleModel = BibleModel(
        version = version,
        downloadedChapters = downloadedChapters,
        downloadStatus = downloadStatus,
        isSelected = isSelected,
        hasPendingUpdate = false,
    )

    private fun TestScope.prepareScenario(
        appLanguage: Language,
        storedVersionId: String? = null,
    ) {
        val preferences = storedVersionId?.let { versionId ->
            mutablePreferencesOf(stringPreferencesKey("selected_bible_version") to versionId)
        } ?: emptyPreferences()
        repository = BibleRepositoryImpl(
            bibleVersionDao = StoredBibleVersionDao(
                listOf(
                    BibleVersionEntity(
                        id = "WEB",
                        status = DownloadStatus.DONE,
                        totalChapters = totalChapters,
                        contentVersion = contentVersion,
                    ),
                    BibleVersionEntity(
                        id = "KJV",
                        status = DownloadStatus.NOT_STARTED,
                        totalChapters = totalChapters,
                        contentVersion = contentVersion,
                    ),
                ),
            ),
            verseDao = CountingVerseDao(
                listOf(
                    VersionChapterCount(
                        bibleVersionId = "WEB",
                        downloadedChapters = totalChapters,
                    ),
                ),
            ),
            bibleVersionRepository = StubBibleVersionRepository(listOf(webVersion, kjvVersion)),
            bibleMapper = BibleMapper(DownloadStatusMapper()),
            dataStore = InMemoryPreferencesDataStore(preferences),
            languageProvider = FixedLanguageProvider(appLanguage),
            observeTableInvalidation = TableInvalidationObserver { flowOf(Unit) },
            applicationScope = ApplicationScope(backgroundScope),
        )
    }
}

private class FixedLanguageProvider(
    private val appLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("Unexpected call")

    override fun getAppLanguage(): Language = appLanguage
}

private class StubBibleVersionRepository(
    private val versions: List<VersionModel>,
) : BibleVersionRepository {
    override suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>> = error("Unexpected call")

    override fun observeVersions(): Flow<List<VersionModel>> = flowOf(versions)
}

private class StoredBibleVersionDao(
    private val versions: List<BibleVersionEntity>,
) : ThrowingBibleVersionDao() {
    override fun getAllVersionsFlow(): Flow<List<BibleVersionEntity>> = flowOf(versions)
}

private class CountingVerseDao(
    private val counts: List<VersionChapterCount>,
) : ThrowingVerseDao() {
    override suspend fun getDownloadedChaptersPerVersion(): List<VersionChapterCount> = counts
}
