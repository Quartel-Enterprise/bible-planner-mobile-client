package com.quare.bibleplanner.feature.themeselection.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.feature.themeselection.data.mapper.ThemePreferenceMapper
import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ThemeSelectionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val mapper: ThemePreferenceMapper,
) : ThemeSelectionRepository {
    override fun getThemeFlow(): Flow<Theme> = dataStore.data.map { value ->
        mapper.mapPreferenceToModel(value[stringPreferencesKey(THEME)])
    }

    override suspend fun setTheme(theme: Theme) {
        dataStore.edit {
            it[stringPreferencesKey(THEME)] = mapper.mapModelToPreference(theme)
        }
    }

    companion object {
        private const val THEME = "theme"
    }
}
