package com.quare.bibleplanner.feature.inappupdate.presentation.model

internal sealed interface UpdateDownloadedUiAction {
    data object NavigateBack : UpdateDownloadedUiAction
}
