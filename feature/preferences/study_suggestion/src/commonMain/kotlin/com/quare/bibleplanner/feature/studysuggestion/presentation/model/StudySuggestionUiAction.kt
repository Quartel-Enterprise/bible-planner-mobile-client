package com.quare.bibleplanner.feature.studysuggestion.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface StudySuggestionUiAction {
    data class ShowSnackbar(
        val message: StringResource,
    ) : StudySuggestionUiAction
}
