package com.quare.bibleplanner.feature.deleteversion.presentation.model

internal sealed interface DeleteVersionUiEvent {
    data object OnConfirmDelete : DeleteVersionUiEvent

    data object OnCancel : DeleteVersionUiEvent
}
