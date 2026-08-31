package com.quare.bibleplanner.feature.studysuggestion.domain.repository

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import kotlinx.coroutines.flow.Flow

interface StudySuggestionSettingsRepository {
    fun observe(): Flow<StudySuggestionSettingsModel>

    suspend fun setEnabled(isEnabled: Boolean)

    suspend fun setMode(mode: StudySuggestionMode)
}
