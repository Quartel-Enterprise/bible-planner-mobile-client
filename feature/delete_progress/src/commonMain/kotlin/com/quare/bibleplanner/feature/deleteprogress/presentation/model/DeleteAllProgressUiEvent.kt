package com.quare.bibleplanner.feature.deleteprogress.presentation.model

sealed interface DeleteAllProgressUiEvent {
    data object OnConfirmDelete : DeleteAllProgressUiEvent

    data object OnCancel : DeleteAllProgressUiEvent
}
