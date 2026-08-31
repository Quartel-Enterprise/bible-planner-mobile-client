package com.quare.bibleplanner.feature.inappupdate.presentation.model

internal sealed interface InAppUpdateDownloadUiAction {
    data object ShowDownloadFailed : InAppUpdateDownloadUiAction
}
