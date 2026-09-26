package com.quare.bibleplanner.core.preferences.studysuggestion.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.preferences.studysuggestion.fake.FakePreferencesDataStore
import com.quare.bibleplanner.core.preferences.studysuggestion.fake.FakeSyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.core.provider.room.entity.SyncedPreferenceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class StudySuggestionSettingsRepositoryImplTest {
    private lateinit var repository: StudySuggestionSettingsRepositoryImpl
    private lateinit var syncedPreferenceDao: FakeSyncedPreferenceDao

    @Test
    fun `GIVEN nothing stored WHEN observing THEN defaults to enabled in dialog mode`() = runTest {
        // Given
        prepareScenario()

        // When
        val settings = repository.observe().first()

        // Then
        assertEquals(
            StudySuggestionSettingsModel(
                isEnabled = true,
                mode = StudySuggestionMode.DIALOG,
            ),
            settings,
        )
    }

    @Test
    fun `GIVEN stored settings WHEN observing THEN reads them back`() = runTest {
        // Given
        prepareScenario(
            preferences = mutablePreferencesOf(
                booleanPreferencesKey(ENABLED_KEY) to false,
                stringPreferencesKey(MODE_KEY) to StudySuggestionMode.BANNER.name,
            ),
        )

        // When
        val settings = repository.observe().first()

        // Then
        assertEquals(
            StudySuggestionSettingsModel(
                isEnabled = false,
                mode = StudySuggestionMode.BANNER,
            ),
            settings,
        )
    }

    @Test
    fun `GIVEN an unknown stored mode WHEN observing THEN falls back to dialog mode`() = runTest {
        // Given
        prepareScenario(preferences = mutablePreferencesOf(stringPreferencesKey(MODE_KEY) to "POPUP"))

        // When
        val settings = repository.observe().first()

        // Then
        assertEquals(StudySuggestionMode.DIALOG, settings.mode)
    }

    @Test
    fun `GIVEN sync disabled WHEN changing the settings THEN stores them locally without mirroring`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.setEnabled(false)
        repository.setMode(StudySuggestionMode.BANNER)

        // Then
        assertEquals(
            StudySuggestionSettingsModel(
                isEnabled = false,
                mode = StudySuggestionMode.BANNER,
            ),
            repository.observe().first(),
        )
        assertTrue(syncedPreferenceDao.localWrites.isEmpty())
    }

    @Test
    fun `GIVEN sync enabled WHEN changing the settings THEN mirrors them into the synced store`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.STUDY_SUGGESTION_SYNC_ENABLED to "true"))

        // When
        repository.setEnabled(false)
        repository.setMode(StudySuggestionMode.BANNER)

        // Then
        assertEquals(
            listOf(
                pendingWrite(
                    key = SyncedPreferenceKeys.STUDY_SUGGESTION_ENABLED,
                    value = "false",
                ),
                pendingWrite(
                    key = SyncedPreferenceKeys.STUDY_SUGGESTION_MODE,
                    value = StudySuggestionMode.BANNER.name,
                ),
            ),
            syncedPreferenceDao.localWrites,
        )
    }

    @Test
    fun `GIVEN no sync flag stored WHEN observing the sync flag THEN reports sync disabled`() = runTest {
        // Given
        prepareScenario()

        // When
        val isSyncEnabled = repository.getSyncEnabledFlow().first()

        // Then
        assertFalse(isSyncEnabled)
    }

    @Test
    fun `GIVEN local settings WHEN turning sync on THEN stores the flag and seeds the current settings`() = runTest {
        // Given
        prepareScenario(
            preferences = mutablePreferencesOf(
                booleanPreferencesKey(ENABLED_KEY) to false,
                stringPreferencesKey(MODE_KEY) to StudySuggestionMode.BANNER.name,
            ),
        )

        // When
        repository.setSyncEnabled(true)

        // Then
        assertEquals(
            listOf(
                pendingWrite(
                    key = SyncedPreferenceKeys.STUDY_SUGGESTION_SYNC_ENABLED,
                    value = "true",
                ),
                pendingWrite(
                    key = SyncedPreferenceKeys.STUDY_SUGGESTION_ENABLED,
                    value = "false",
                ),
                pendingWrite(
                    key = SyncedPreferenceKeys.STUDY_SUGGESTION_MODE,
                    value = StudySuggestionMode.BANNER.name,
                ),
            ),
            syncedPreferenceDao.localWrites,
        )
        assertTrue(repository.getSyncEnabledFlow().first())
    }

    @Test
    fun `GIVEN sync on WHEN turning sync off THEN only stores the flag`() = runTest {
        // Given
        prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.STUDY_SUGGESTION_SYNC_ENABLED to "true"))

        // When
        repository.setSyncEnabled(false)

        // Then
        assertEquals(
            listOf(
                pendingWrite(
                    key = SyncedPreferenceKeys.STUDY_SUGGESTION_SYNC_ENABLED,
                    value = "false",
                ),
            ),
            syncedPreferenceDao.localWrites,
        )
    }

    @Test
    fun `GIVEN synced values WHEN observing them THEN parses the enabled flag and the mode`() = runTest {
        // Given
        prepareScenario(
            syncedValues = mapOf(
                SyncedPreferenceKeys.STUDY_SUGGESTION_ENABLED to "false",
                SyncedPreferenceKeys.STUDY_SUGGESTION_MODE to StudySuggestionMode.BANNER.name,
            ),
        )

        // When
        val syncedEnabled = repository.observeSyncedEnabled().first()
        val syncedMode = repository.observeSyncedMode().first()

        // Then
        assertEquals(false, syncedEnabled)
        assertEquals(StudySuggestionMode.BANNER, syncedMode)
    }

    @Test
    fun `GIVEN malformed synced values WHEN observing them THEN reports them as missing`() = runTest {
        // Given
        prepareScenario(
            syncedValues = mapOf(
                SyncedPreferenceKeys.STUDY_SUGGESTION_ENABLED to "maybe",
                SyncedPreferenceKeys.STUDY_SUGGESTION_MODE to "POPUP",
            ),
        )

        // When
        val syncedEnabled = repository.observeSyncedEnabled().first()
        val syncedMode = repository.observeSyncedMode().first()

        // Then
        assertNull(syncedEnabled)
        assertNull(syncedMode)
    }

    @Test
    fun `GIVEN sync enabled WHEN applying remote values THEN stores them locally without echoing them back`() =
        runTest {
            // Given
            prepareScenario(syncedValues = mapOf(SyncedPreferenceKeys.STUDY_SUGGESTION_SYNC_ENABLED to "true"))

            // When
            repository.applySyncedEnabled(false)
            repository.applySyncedMode(StudySuggestionMode.BANNER)

            // Then
            assertEquals(
                StudySuggestionSettingsModel(
                    isEnabled = false,
                    mode = StudySuggestionMode.BANNER,
                ),
                repository.observe().first(),
            )
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
        repository = StudySuggestionSettingsRepositoryImpl(
            dataStore = FakePreferencesDataStore(preferences),
            syncedPreferenceDao = syncedPreferenceDao,
            currentTimestampProvider = { NOW },
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
        const val ENABLED_KEY = "study_suggestion_enabled"
        const val MODE_KEY = "study_suggestion_mode"
    }
}
