package com.quare.bibleplanner.feature.studysuggestion.domain.repository

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import kotlinx.coroutines.flow.Flow

interface StudySuggestionSettingsRepository {
    fun observe(): Flow<StudySuggestionSettingsModel>

    suspend fun setEnabled(isEnabled: Boolean)

    suspend fun setMode(mode: StudySuggestionMode)

    fun getSyncEnabledFlow(): Flow<Boolean>

    suspend fun setSyncEnabled(enabled: Boolean)

    fun observeSyncedEnabled(): Flow<Boolean?>

    fun observeSyncedMode(): Flow<StudySuggestionMode?>

    suspend fun applySyncedEnabled(isEnabled: Boolean)

    suspend fun applySyncedMode(mode: StudySuggestionMode)
}
