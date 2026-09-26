package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import kotlinx.coroutines.flow.Flow

fun interface ObserveStudySuggestionSettings {
    operator fun invoke(): Flow<StudySuggestionSettingsModel>
}
