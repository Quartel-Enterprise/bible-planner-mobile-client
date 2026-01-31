package com.quare.bibleplanner.core.books.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.model.BibleVersion
import com.quare.bibleplanner.core.utils.locale.AppLanguage
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BibleVersionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : BibleVersionRepository {
    override fun getSelectedVersionAbbreviationFlow(): Flow<String> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(BIBLE_VERSION_KEY)] ?: getDefaultVersion().name
    }

    override suspend fun setSelectedVersionAbbreviation(abbreviation: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(BIBLE_VERSION_KEY)] = abbreviation
        }
    }

    private fun getDefaultVersion(): BibleVersion = if (getCurrentLanguage() == AppLanguage.PORTUGUESE_BRAZIL) {
        BibleVersion.ACF
    } else {
        BibleVersion.WEB
    }

    companion object {
        private const val BIBLE_VERSION_KEY = "selected_bible_version"
    }
}
