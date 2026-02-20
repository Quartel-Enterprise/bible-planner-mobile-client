package com.quare.bibleplanner.feature.bibleversion.presentation.model

sealed interface BibleVersionUiAction {
    data object BackToPreviousRoute : BibleVersionUiAction

    data class NavigateToRoute(
        val route: Any,
    ) : BibleVersionUiAction
}
