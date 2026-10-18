package com.quare.bibleplanner.feature.studysuggestion.presentation.model

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import software.quare.loadable.Loadable

internal data class StudySuggestionUiState(
    val settings: Loadable<StudySuggestionSettingsModel>,
    val isSyncEnabled: Boolean,
    val isLoggedIn: Boolean,
)
