package com.quare.bibleplanner.core.inappupdate.domain.model

sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState

    data class Downloading(
        val progress: Int,
    ) : UpdateDownloadState

    data object Downloaded : UpdateDownloadState

    data object Failed : UpdateDownloadState
}
