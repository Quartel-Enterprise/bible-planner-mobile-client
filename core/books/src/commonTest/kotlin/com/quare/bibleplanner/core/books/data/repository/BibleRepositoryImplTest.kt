package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.mapper.BibleMapper
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.books.fake.ThrowingBibleVersionDao
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.room.invalidation.TableInvalidationObserver
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BibleRepositoryImplTest {
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

    private fun prepareScenario(
        appLanguage: Language,
        storedVersionId: String? = null,
    ) {
        val preferences = storedVersionId?.let { versionId ->
            mutablePreferencesOf(stringPreferencesKey("selected_bible_version") to versionId)
        } ?: emptyPreferences()
        repository = BibleRepositoryImpl(
            bibleVersionDao = ThrowingBibleVersionDao(),
            verseDao = ThrowingVerseDao(),
            bibleVersionRepository = StubBibleVersionRepository(),
            bibleMapper = BibleMapper(DownloadStatusMapper()),
            dataStore = FakePreferencesDataStore(preferences),
            languageProvider = FixedLanguageProvider(appLanguage),
            observeTableInvalidation = TableInvalidationObserver { emptyFlow() },
            applicationScope = ApplicationScope(),
        )
    }
}

private class FakePreferencesDataStore(
    private val preferences: Preferences,
) : DataStore<Preferences> {
    override val data: Flow<Preferences> = flowOf(preferences)

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
        error("Unexpected call")
}

private class FixedLanguageProvider(
    private val appLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("Unexpected call")

    override fun getAppLanguage(): Language = appLanguage
}

private class StubBibleVersionRepository : BibleVersionRepository {
    override suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>> = error("Unexpected call")

    override fun observeVersions(): Flow<List<VersionModel>> = error("Unexpected call")
}
