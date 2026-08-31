package com.quare.bibleplanner.feature.studysuggestion.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel

internal data class StudySuggestionUiState(
    val settings: Loadable<StudySuggestionSettingsModel>,
    val isSyncEnabled: Boolean,
    val isLoggedIn: Boolean,
)
