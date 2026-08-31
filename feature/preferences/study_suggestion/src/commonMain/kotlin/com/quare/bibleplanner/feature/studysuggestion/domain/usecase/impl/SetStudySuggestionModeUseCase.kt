package com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.SetStudySuggestionMode

internal class SetStudySuggestionModeUseCase(
    private val repository: StudySuggestionSettingsRepository,
) : SetStudySuggestionMode {
    override suspend fun invoke(mode: StudySuggestionMode) = repository.setMode(mode)
}
