package com.quare.bibleplanner.feature.contactsupport.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface ContactSupportUiAction {
    data object NavigateBack : ContactSupportUiAction

    data class OpenLink(
        val url: String,
    ) : ContactSupportUiAction

    data class Copy(
        val text: String,
    ) : ContactSupportUiAction

    data class ShowSnackbar(
        val message: StringResource,
    ) : ContactSupportUiAction
}
