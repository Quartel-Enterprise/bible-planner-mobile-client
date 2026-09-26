package com.quare.bibleplanner.core.provider.language.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.provider.language.data.mapper.AppLanguageMapper
import com.quare.bibleplanner.core.provider.language.fake.FakePreferencesDataStore
import com.quare.bibleplanner.core.provider.language.fake.FakeSyncedPreferenceDao
import com.quare.bibleplanner.core.provider.language.fake.FixedLanguageProvider
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AppLanguageRepositoryImplTest {
    private lateinit var repository: AppLanguageRepositoryImpl
    private lateinit var syncedPreferenceDao: FakeSyncedPreferenceDao

    @Test
    fun `GIVEN no stored language WHEN observing it THEN follows the app language`() = runTest {
        // Given
        prepareScenario()

        // When
        val language = repository.getLanguageFlow().first()

        // Then
        assertEquals(Language.ENGLISH, language)
    }

    @Test
    fun `GIVEN a stored language WHEN observing it THEN returns the stored language`() = runTest {
        // Given
        prepareScenario(preferences = mutablePreferencesOf(stringPreferencesKey(LANGUAGE_KEY) to "pt-BR"))

        // When
        val language = repository.getLanguageFlow().first()

        // Then
        assertEquals(Language.PORTUGUESE_BRAZIL, language)
    }

    @Test
    fun `GIVEN language sync off WHEN changing the language THEN stores it without mirroring`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.setLanguage(Language.SPANISH)

        // Then
        assertEquals(Language.SPANISH, repository.getLanguageFlow().first())
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    @Test
    fun `GIVEN language sync on WHEN changing the language THEN mirrors it into the synced store`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.LANGUAGE_SYNC_ENABLED to "true"))

        // When
        repository.setLanguage(Language.SPANISH)

        // Then
        assertEquals(
            listOf(
                pendingWrite(
                    key = SyncedPreferenceKeys.APP_LANGUAGE,
                    value = "es",
                ),
            ),
            syncedPreferenceDao.localWrites,
        )
    }

    @Test
    fun `GIVEN no sync flag WHEN observing it THEN reports sync disabled`() = runTest {
        // Given
        prepareScenario()

        // When
        val isSyncEnabled = repository.getLanguageSyncEnabledFlow().first()

        // Then
        assertFalse(isSyncEnabled)
    }

    @Test
    fun `GIVEN a stored language WHEN turning sync on THEN stores the flag and seeds the current language`() = runTest {
        // Given
        prepareScenario(preferences = mutablePreferencesOf(stringPreferencesKey(LANGUAGE_KEY) to "pt-BR"))

        // When
        repository.setLanguageSyncEnabled(true)

        // Then
        assertEquals(
            listOf(
                pendingWrite(
                    key = SyncedPreferenceKeys.LANGUAGE_SYNC_ENABLED,
                    value = "true",
                ),
                pendingWrite(
                    key = SyncedPreferenceKeys.APP_LANGUAGE,
                    value = "pt-BR",
                ),
            ),
            syncedPreferenceDao.localWrites,
        )
    }

    @Test
    fun `GIVEN sync on WHEN turning sync off THEN only stores the flag`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.LANGUAGE_SYNC_ENABLED to "true"))

        // When
        repository.setLanguageSyncEnabled(false)

        // Then
        assertEquals(
            listOf(
                pendingWrite(
                    key = SyncedPreferenceKeys.LANGUAGE_SYNC_ENABLED,
                    value = "false",
                ),
            ),
            syncedPreferenceDao.localWrites,
        )
        assertFalse(repository.getLanguageSyncEnabledFlow().first())
    }

    @Test
    fun `GIVEN a synced language WHEN observing it THEN maps the synced tag`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.APP_LANGUAGE to "es"))

        // When
        val language = repository.observeSyncedLanguage().first()

        // Then
        assertEquals(Language.SPANISH, language)
    }

    @Test
    fun `GIVEN no synced language WHEN observing it THEN emits null`() = runTest {
        // Given
        prepareScenario()

        // When
        val language = repository.observeSyncedLanguage().first()

        // Then
        assertNull(language)
    }

    @Test
    fun `GIVEN sync on WHEN applying a remote language THEN stores it without echoing it back`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.LANGUAGE_SYNC_ENABLED to "true"))

        // When
        repository.applySyncedLanguage(Language.PORTUGUESE_BRAZIL)

        // Then
        assertEquals(Language.PORTUGUESE_BRAZIL, repository.getLanguageFlow().first())
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    private fun pendingWrite(
        key: String,
        value: String,
    ) = SyncedPreferenceEntity(
        key = key,
        value = value,
        updatedAt = NOW,
        pendingSync = true,
    )

    private fun prepareScenario(
        preferences: Preferences = emptyPreferences(),
        syncedValues: Map<String, String> = emptyMap(),
    ) {
        syncedPreferenceDao = FakeSyncedPreferenceDao(syncedValues)
        repository = AppLanguageRepositoryImpl(
            dataStore = FakePreferencesDataStore(preferences),
            mapper = AppLanguageMapper(FixedLanguageProvider(Language.ENGLISH)),
            syncedPreferenceDao = syncedPreferenceDao,
            currentTimestampProvider = { NOW },
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
        const val LANGUAGE_KEY = "app_language"
    }
}
