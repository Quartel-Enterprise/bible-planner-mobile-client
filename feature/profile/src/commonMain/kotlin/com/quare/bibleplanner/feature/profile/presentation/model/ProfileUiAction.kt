package com.quare.bibleplanner.feature.profile.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface ProfileUiAction {
    data class OpenLink(
        val url: String,
    ) : ProfileUiAction

    data object ShowNoProgressToDelete : ProfileUiAction

    data class ShowSnackbar(
        val message: StringResource,
    ) : ProfileUiAction

    data class Copy(
        val text: String,
    ) : ProfileUiAction
}
