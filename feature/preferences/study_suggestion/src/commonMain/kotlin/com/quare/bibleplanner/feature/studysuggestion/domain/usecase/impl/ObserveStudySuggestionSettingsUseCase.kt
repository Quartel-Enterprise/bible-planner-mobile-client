package com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.ObserveStudySuggestionSettings
import kotlinx.coroutines.flow.Flow

internal class ObserveStudySuggestionSettingsUseCase(
    private val repository: StudySuggestionSettingsRepository,
) : ObserveStudySuggestionSettings {
    override fun invoke(): Flow<StudySuggestionSettingsModel> = repository.observe()
}
