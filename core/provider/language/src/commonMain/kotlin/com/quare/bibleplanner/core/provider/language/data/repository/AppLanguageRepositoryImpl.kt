package com.quare.bibleplanner.core.provider.language.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.provider.language.data.mapper.AppLanguageMapper
import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AppLanguageRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val mapper: AppLanguageMapper,
) : AppLanguageRepository {
    override fun getLanguageFlow(): Flow<Language> = dataStore.data.map { preferences ->
        mapper.mapPreferenceToModel(preferences[stringPreferencesKey(APP_LANGUAGE)])
    }

    override suspend fun setLanguage(language: Language) {
        dataStore.edit {
            it[stringPreferencesKey(APP_LANGUAGE)] = mapper.mapModelToPreference(language)
        }
    }

    companion object {
        private const val APP_LANGUAGE = "app_language"
    }
}
