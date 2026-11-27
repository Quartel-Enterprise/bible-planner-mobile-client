package com.quare.bibleplanner.feature.profile.presentation.model

internal sealed interface ProfileUiAction {
    data class OpenLink(
        val url: String,
    ) : ProfileUiAction

    data object ShowNoProgressToDelete : ProfileUiAction

    data class ShowSnackbar(
        val message: ProfileMessage,
    ) : ProfileUiAction

    data class Copy(
        val text: String,
    ) : ProfileUiAction
}
