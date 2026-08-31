package com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.SetStudySuggestionEnabled

internal class SetStudySuggestionEnabledUseCase(
    private val repository: StudySuggestionSettingsRepository,
) : SetStudySuggestionEnabled {
    override suspend fun invoke(isEnabled: Boolean) = repository.setEnabled(isEnabled)
}
