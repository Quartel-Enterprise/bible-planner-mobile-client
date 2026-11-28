package com.quare.bibleplanner.feature.contactsupport.presentation.model

internal sealed interface ContactSupportUiAction {
    data class OpenLink(
        val url: String,
    ) : ContactSupportUiAction

    data class Copy(
        val text: String,
    ) : ContactSupportUiAction

    data object NotifyEmailCopied : ContactSupportUiAction
}
