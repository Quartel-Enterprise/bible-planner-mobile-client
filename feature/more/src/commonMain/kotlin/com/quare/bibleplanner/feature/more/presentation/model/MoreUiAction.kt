package com.quare.bibleplanner.feature.more.presentation.model

internal sealed interface MoreUiAction {
    data object GoToTheme : MoreUiAction

    data object GoToPaywall : MoreUiAction

    data class OpenLink(
        val url: String,
    ) : MoreUiAction

    data object GoToEditPlanStartDay : MoreUiAction

    data object GoToDeleteProgress : MoreUiAction

    data object ShowNoProgressToDelete : MoreUiAction
}
