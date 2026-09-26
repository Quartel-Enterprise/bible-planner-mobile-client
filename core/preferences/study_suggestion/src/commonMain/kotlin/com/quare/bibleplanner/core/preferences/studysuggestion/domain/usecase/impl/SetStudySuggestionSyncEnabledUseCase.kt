package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionSyncEnabled

internal class SetStudySuggestionSyncEnabledUseCase(
    private val repository: StudySuggestionSettingsRepository,
) : SetStudySuggestionSyncEnabled {
    override suspend fun invoke(enabled: Boolean) = repository.setSyncEnabled(enabled)
}
