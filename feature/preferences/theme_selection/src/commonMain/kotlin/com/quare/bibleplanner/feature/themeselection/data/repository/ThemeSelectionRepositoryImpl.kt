package com.quare.bibleplanner.feature.themeselection.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.feature.themeselection.data.mapper.ThemePreferenceMapper
import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Theme + contrast live in the device-local DataStore (the render source). When the account-global
 * sync flag is on, user writes are also mirrored into the synced key-value store
 * ([SyncedPreferenceDao]) so the sync engine pushes them; inbound remote values are written back
 * through the `applySynced*` methods, which skip the mirror to avoid an echo loop.
 */
internal class ThemeSelectionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val mapper: ThemePreferenceMapper,
    private val syncedPreferenceDao: SyncedPreferenceDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : ThemeSelectionRepository {
    override fun getThemeFlow(): Flow<Theme> = dataStore.data.map { value ->
        mapper.mapPreferenceToModel(value[stringPreferencesKey(THEME)])
    }

    override suspend fun setTheme(theme: Theme) {
        writeTheme(theme)
        mirrorIfSyncEnabled(
            key = SyncedPreferenceKeys.APP_THEME,
            value = mapper.mapModelToPreference(theme),
        )
    }

    override fun getContrastTypeFlow(): Flow<ContrastType> = dataStore.data.map { preferences ->
        mapper.mapContrastPreferenceToModel(preferences[stringPreferencesKey(CONTRAST)])
    }

    override suspend fun setContrastType(contrastType: ContrastType) {
        writeContrast(contrastType)
        mirrorIfSyncEnabled(
            key = SyncedPreferenceKeys.THEME_CONTRAST,
            value = mapper.mapModelToContrastPreference(contrastType),
        )
    }

    override fun getThemeSyncEnabledFlow(): Flow<Boolean> =
        syncedPreferenceDao.observeValue(SyncedPreferenceKeys.THEME_SYNC_ENABLED).map { it.toBoolean() }

    override suspend fun setThemeSyncEnabled(enabled: Boolean) {
        val now = currentTimestampProvider.getCurrentTimestamp()
        syncedPreferenceDao.setLocal(
            key = SyncedPreferenceKeys.THEME_SYNC_ENABLED,
            value = enabled.toString(),
            updatedAt = now,
        )
        if (enabled) {
            syncedPreferenceDao.setLocal(
                key = SyncedPreferenceKeys.APP_THEME,
                value = mapper.mapModelToPreference(getThemeFlow().first()),
                updatedAt = now,
            )
            syncedPreferenceDao.setLocal(
                key = SyncedPreferenceKeys.THEME_CONTRAST,
                value = mapper.mapModelToContrastPreference(getContrastTypeFlow().first()),
                updatedAt = now,
            )
        }
    }

    override fun observeSyncedTheme(): Flow<Theme?> =
        syncedPreferenceDao.observeValue(SyncedPreferenceKeys.APP_THEME).map { value ->
            value?.let(mapper::mapPreferenceToModel)
        }

    override fun observeSyncedContrast(): Flow<ContrastType?> =
        syncedPreferenceDao.observeValue(SyncedPreferenceKeys.THEME_CONTRAST).map { value ->
            value?.let(mapper::mapContrastPreferenceToModel)
        }

    override suspend fun applySyncedTheme(theme: Theme) = writeTheme(theme)

    override suspend fun applySyncedContrast(contrastType: ContrastType) = writeContrast(contrastType)

    private suspend fun writeTheme(theme: Theme) = dataStore.write(
        key = stringPreferencesKey(THEME),
        value = mapper.mapModelToPreference(theme),
    )

    private suspend fun writeContrast(contrastType: ContrastType) = dataStore.write(
        key = stringPreferencesKey(CONTRAST),
        value = mapper.mapModelToContrastPreference(contrastType),
    )

    private suspend fun mirrorIfSyncEnabled(
        key: String,
        value: String,
    ) {
        if (getThemeSyncEnabledFlow().first()) {
            syncedPreferenceDao.setLocal(
                key = key,
                value = value,
                updatedAt = currentTimestampProvider.getCurrentTimestamp(),
            )
        }
    }

    companion object {
        private const val THEME = "theme"
        private const val CONTRAST = "contrast"
    }
}
