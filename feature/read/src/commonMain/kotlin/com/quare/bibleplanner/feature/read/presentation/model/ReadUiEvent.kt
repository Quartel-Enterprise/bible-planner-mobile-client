package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel

/**
 * Represents actions that can be performed by the user on the Read screen.
 */
sealed interface ReadUiEvent {
    /**
     * Event triggered when the user wants to navigate back.
     */
    data object OnArrowBackClick : ReadUiEvent

    /**
     * Event triggered to retry loading the chapter content if it failed.
     */
    data object OnRetryClick : ReadUiEvent

    /**
     * Event triggered to toggle the read status of the current chapter.
     */
    data object ToggleReadStatus : ReadUiEvent

    /**
     * Event triggered when the user wants to change the Bible version.
     */
    data object ManageBibleVersions : ReadUiEvent

    data class OnNavigationSuggestionClick(val suggestion: ReadNavigationSuggestionModel) : ReadUiEvent
}
