package com.quare.bibleplanner.feature.inappupdate.presentation.model

internal sealed interface UpdateDownloadedUiEvent {
    data object OnRestartNowClick : UpdateDownloadedUiEvent

    data object OnLaterClick : UpdateDownloadedUiEvent
}
