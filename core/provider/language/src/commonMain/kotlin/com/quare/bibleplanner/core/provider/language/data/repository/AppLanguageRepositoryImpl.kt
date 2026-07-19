package com.quare.bibleplanner.core.provider.language.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.language.data.mapper.AppLanguageMapper
import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * The language lives in the device-local DataStore (the render source). When the account-global sync
 * flag is on, a user write is also mirrored into the synced key-value store ([SyncedPreferenceDao]);
 * inbound remote values are written back through [applySyncedLanguage], which skips the mirror to
 * avoid an echo loop.
 */
internal class AppLanguageRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val mapper: AppLanguageMapper,
    private val syncedPreferenceDao: SyncedPreferenceDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : AppLanguageRepository {
    override fun getLanguageFlow(): Flow<Language> = dataStore.data.map { preferences ->
        mapper.mapPreferenceToModel(preferences[stringPreferencesKey(APP_LANGUAGE)])
    }

    override suspend fun setLanguage(language: Language) {
        writeLanguage(language)
        if (getLanguageSyncEnabledFlow().first()) {
            syncedPreferenceDao.setLocal(
                key = SyncedPreferenceKeys.APP_LANGUAGE,
                value = mapper.mapModelToPreference(language),
                updatedAt = currentTimestampProvider.getCurrentTimestamp(),
            )
        }
    }

    override fun getLanguageSyncEnabledFlow(): Flow<Boolean> =
        syncedPreferenceDao.observeValue(SyncedPreferenceKeys.LANGUAGE_SYNC_ENABLED).map { it.toBoolean() }

    override suspend fun setLanguageSyncEnabled(enabled: Boolean) {
        val now = currentTimestampProvider.getCurrentTimestamp()
        syncedPreferenceDao.setLocal(
            key = SyncedPreferenceKeys.LANGUAGE_SYNC_ENABLED,
            value = enabled.toString(),
            updatedAt = now,
        )
        if (enabled) {
            syncedPreferenceDao.setLocal(
                key = SyncedPreferenceKeys.APP_LANGUAGE,
                value = mapper.mapModelToPreference(getLanguageFlow().first()),
                updatedAt = now,
            )
        }
    }

    override fun observeSyncedLanguage(): Flow<Language?> =
        syncedPreferenceDao.observeValue(SyncedPreferenceKeys.APP_LANGUAGE).map { value ->
            value?.let(mapper::mapPreferenceToModel)
        }

    override suspend fun applySyncedLanguage(language: Language) = writeLanguage(language)

    private suspend fun writeLanguage(language: Language) = dataStore.write(
        key = stringPreferencesKey(APP_LANGUAGE),
        value = mapper.mapModelToPreference(language),
    )

    companion object {
        private const val APP_LANGUAGE = "app_language"
    }
}
