package com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.GetStudySuggestionSyncEnabledFlow
import kotlinx.coroutines.flow.Flow

internal class GetStudySuggestionSyncEnabledFlowUseCase(
    private val repository: StudySuggestionSettingsRepository,
) : GetStudySuggestionSyncEnabledFlow {
    override fun invoke(): Flow<Boolean> = repository.getSyncEnabledFlow()
}
