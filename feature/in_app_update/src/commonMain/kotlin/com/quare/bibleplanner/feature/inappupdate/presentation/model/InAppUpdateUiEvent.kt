package com.quare.bibleplanner.feature.inappupdate.presentation.model

internal sealed interface InAppUpdateUiEvent {
    data object OnUpdateClick : InAppUpdateUiEvent

    data object OnDismiss : InAppUpdateUiEvent
}
