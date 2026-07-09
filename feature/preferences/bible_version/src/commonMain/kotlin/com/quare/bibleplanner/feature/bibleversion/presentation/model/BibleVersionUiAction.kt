package com.quare.bibleplanner.feature.bibleversion.presentation.model

import androidx.navigation3.runtime.NavKey

sealed interface BibleVersionUiAction {
    data object BackToPreviousRoute : BibleVersionUiAction

    data class NavigateToRoute(
        val route: NavKey,
    ) : BibleVersionUiAction

    data object ShowDownloadTip : BibleVersionUiAction
}
