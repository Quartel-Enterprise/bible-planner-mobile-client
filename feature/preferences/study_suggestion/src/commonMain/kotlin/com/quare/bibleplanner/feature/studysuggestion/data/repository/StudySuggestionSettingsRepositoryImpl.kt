package com.quare.bibleplanner.feature.studysuggestion.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.datastore.write
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class StudySuggestionSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : StudySuggestionSettingsRepository {
    override fun observe(): Flow<StudySuggestionSettingsModel> = dataStore.data.map { preferences ->
        StudySuggestionSettingsModel(
            isEnabled = preferences[ENABLED_KEY] != false,
            mode = preferences[MODE_KEY]?.toMode() ?: StudySuggestionMode.DIALOG,
        )
    }

    override suspend fun setEnabled(isEnabled: Boolean) = dataStore.write(
        key = ENABLED_KEY,
        value = isEnabled,
    )

    override suspend fun setMode(mode: StudySuggestionMode) = dataStore.write(
        key = MODE_KEY,
        value = mode.name,
    )

    private fun String.toMode(): StudySuggestionMode? = StudySuggestionMode.entries.find { it.name == this }

    private companion object {
        val ENABLED_KEY = booleanPreferencesKey("study_suggestion_enabled")
        val MODE_KEY = stringPreferencesKey("study_suggestion_mode")
    }
}
