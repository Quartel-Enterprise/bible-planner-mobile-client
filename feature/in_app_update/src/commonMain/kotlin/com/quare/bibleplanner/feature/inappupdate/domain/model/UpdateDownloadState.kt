package com.quare.bibleplanner.feature.inappupdate.domain.model

sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState

    data class Downloading(
        val progress: Int,
    ) : UpdateDownloadState

    data object Downloaded : UpdateDownloadState

    data object Failed : UpdateDownloadState
}
