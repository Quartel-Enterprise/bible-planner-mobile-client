package com.quare.bibleplanner.feature.studysuggestion.presentation.factory

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.GetStudySuggestionSyncEnabledFlow
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.ObserveStudySuggestionSettings
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import software.quare.loadable.Loadable
import software.quare.loadable.toLoaded

internal class StudySuggestionUiStateFactory(
    private val observeStudySuggestionSettings: ObserveStudySuggestionSettings,
    private val getStudySuggestionSyncEnabledFlow: GetStudySuggestionSyncEnabledFlow,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
) {
    fun createInitialState(): StudySuggestionUiState = StudySuggestionUiState(
        settings = Loadable.Loading,
        isSyncEnabled = false,
        isLoggedIn = false,
    )

    fun create(): Flow<StudySuggestionUiState> = combine(
        observeStudySuggestionSettings(),
        getStudySuggestionSyncEnabledFlow(),
        observeAuthenticatedUserId(),
    ) { settings, isSyncEnabled, userId ->
        StudySuggestionUiState(
            settings = settings.toLoaded(),
            isSyncEnabled = isSyncEnabled,
            isLoggedIn = userId != null,
        )
    }
}
