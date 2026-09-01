package com.quare.bibleplanner.feature.studysuggestion.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * The settings live in the device-local DataStore (the render source). When the account-global sync
 * flag is on, user writes are also mirrored into the synced key-value store ([SyncedPreferenceDao])
 * so the sync engine pushes them; inbound remote values are written back through the `applySynced*`
 * methods, which skip the mirror to avoid an echo loop.
 */
internal class StudySuggestionSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val syncedPreferenceDao: SyncedPreferenceDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : StudySuggestionSettingsRepository {
    override fun observe(): Flow<StudySuggestionSettingsModel> = dataStore.data.map { preferences ->
        StudySuggestionSettingsModel(
            isEnabled = preferences[ENABLED_KEY] != false,
            mode = preferences[MODE_KEY]?.toMode() ?: StudySuggestionMode.DIALOG,
        )
    }

    override suspend fun setEnabled(isEnabled: Boolean) {
        writeEnabled(isEnabled)
        mirrorIfSyncEnabled(
            key = SyncedPreferenceKeys.STUDY_SUGGESTION_ENABLED,
            value = isEnabled.toString(),
        )
    }

    override suspend fun setMode(mode: StudySuggestionMode) {
        writeMode(mode)
        mirrorIfSyncEnabled(
            key = SyncedPreferenceKeys.STUDY_SUGGESTION_MODE,
            value = mode.name,
        )
    }

    override fun getSyncEnabledFlow(): Flow<Boolean> = syncedPreferenceDao
        .observeValue(SyncedPreferenceKeys.STUDY_SUGGESTION_SYNC_ENABLED)
        .map { it.toBoolean() }

    override suspend fun setSyncEnabled(enabled: Boolean) {
        val now = currentTimestampProvider.getCurrentTimestamp()
        syncedPreferenceDao.setLocal(
            key = SyncedPreferenceKeys.STUDY_SUGGESTION_SYNC_ENABLED,
            value = enabled.toString(),
            updatedAt = now,
        )
        if (enabled) {
            val settings = observe().first()
            syncedPreferenceDao.setLocal(
                key = SyncedPreferenceKeys.STUDY_SUGGESTION_ENABLED,
                value = settings.isEnabled.toString(),
                updatedAt = now,
            )
            syncedPreferenceDao.setLocal(
                key = SyncedPreferenceKeys.STUDY_SUGGESTION_MODE,
                value = settings.mode.name,
                updatedAt = now,
            )
        }
    }

    override fun observeSyncedEnabled(): Flow<Boolean?> = syncedPreferenceDao
        .observeValue(SyncedPreferenceKeys.STUDY_SUGGESTION_ENABLED)
        .map { value -> value?.toBooleanStrictOrNull() }

    override fun observeSyncedMode(): Flow<StudySuggestionMode?> = syncedPreferenceDao
        .observeValue(SyncedPreferenceKeys.STUDY_SUGGESTION_MODE)
        .map { value -> value?.toMode() }

    override suspend fun applySyncedEnabled(isEnabled: Boolean) = writeEnabled(isEnabled)

    override suspend fun applySyncedMode(mode: StudySuggestionMode) = writeMode(mode)

    private suspend fun writeEnabled(isEnabled: Boolean) = dataStore.write(
        key = ENABLED_KEY,
        value = isEnabled,
    )

    private suspend fun writeMode(mode: StudySuggestionMode) = dataStore.write(
        key = MODE_KEY,
        value = mode.name,
    )

    private suspend fun mirrorIfSyncEnabled(
        key: String,
        value: String,
    ) {
        if (getSyncEnabledFlow().first()) {
            syncedPreferenceDao.setLocal(
                key = key,
                value = value,
                updatedAt = currentTimestampProvider.getCurrentTimestamp(),
            )
        }
    }

    private fun String.toMode(): StudySuggestionMode? = StudySuggestionMode.entries.find { it.name == this }

    private companion object {
        val ENABLED_KEY = booleanPreferencesKey("study_suggestion_enabled")
        val MODE_KEY = stringPreferencesKey("study_suggestion_mode")
    }
}
