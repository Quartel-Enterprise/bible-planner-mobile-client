package com.quare.bibleplanner.feature.studysuggestion.domain.usecase

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import kotlinx.coroutines.flow.Flow

fun interface ObserveStudySuggestionSettings {
    operator fun invoke(): Flow<StudySuggestionSettingsModel>
}
