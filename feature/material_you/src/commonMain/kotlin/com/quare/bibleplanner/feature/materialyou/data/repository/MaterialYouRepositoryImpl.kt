package com.quare.bibleplanner.feature.materialyou.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.quare.bibleplanner.core.utils.orTrue
import com.quare.bibleplanner.feature.materialyou.domain.repository.MaterialYouRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MaterialYouRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : MaterialYouRepository {
    override fun getIsDynamicColorsEnabledFlow(): Flow<Boolean> = dataStore.data.map {
        it[booleanPreferencesKey(IS_DYNAMIC_COLORS_ENABLED_KEY)].orTrue()
    }

    override suspend fun setIsDynamicColorsEnabled(isEnabled: Boolean) {
        dataStore.edit {
            it[booleanPreferencesKey(IS_DYNAMIC_COLORS_ENABLED_KEY)] = isEnabled
        }
    }

    companion object {
        private const val IS_DYNAMIC_COLORS_ENABLED_KEY = "is_dynamic_colors_enabled"
    }
}
