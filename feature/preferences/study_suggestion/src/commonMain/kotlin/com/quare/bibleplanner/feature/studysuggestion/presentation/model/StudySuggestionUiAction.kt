package com.quare.bibleplanner.feature.studysuggestion.presentation.model

internal sealed interface StudySuggestionUiAction {
    data class ShowSnackbar(
        val message: StudySuggestionMessage,
    ) : StudySuggestionUiAction
}
