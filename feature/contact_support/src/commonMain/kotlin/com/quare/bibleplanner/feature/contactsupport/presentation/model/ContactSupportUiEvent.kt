package com.quare.bibleplanner.feature.contactsupport.presentation.model

internal sealed interface ContactSupportUiEvent {
    data object OnDismiss : ContactSupportUiEvent

    data object OnSendEmailClick : ContactSupportUiEvent

    data object OnCopyEmailClick : ContactSupportUiEvent
}
