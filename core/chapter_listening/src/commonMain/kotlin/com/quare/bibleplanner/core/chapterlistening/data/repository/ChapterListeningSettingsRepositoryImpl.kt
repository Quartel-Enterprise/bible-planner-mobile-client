package com.quare.bibleplanner.core.chapterlistening.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.datastore.write
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ChapterListeningSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ChapterListeningSettingsRepository {
    private val speedKey = floatPreferencesKey("listening_speed")
    private val voiceIdKey = stringPreferencesKey("listening_voice_id")
    private val autoNextEnabledKey = booleanPreferencesKey("listening_auto_next_enabled")

    override fun observe(): Flow<ChapterListeningSettingsModel> = dataStore.data.map { preferences ->
        ChapterListeningSettingsModel(
            speed = preferences[speedKey] ?: ChapterListeningSettingsModel.DEFAULT_SPEED,
            voiceId = preferences[voiceIdKey],
            isAutoNextEnabled = preferences[autoNextEnabledKey] != false,
        )
    }

    override suspend fun setSpeed(speed: Float) {
        dataStore.write(
            key = speedKey,
            value = speed,
        )
    }

    override suspend fun setVoiceId(voiceId: String) {
        dataStore.write(
            key = voiceIdKey,
            value = voiceId,
        )
    }

    override suspend fun setAutoNextEnabled(isEnabled: Boolean) {
        dataStore.write(
            key = autoNextEnabledKey,
            value = isEnabled,
        )
    }
}
