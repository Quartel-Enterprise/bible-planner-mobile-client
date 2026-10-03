package com.quare.bibleplanner.core.model.downloadstatus

sealed interface DownloadStatusModel {
    data object NotStarted : DownloadStatusModel

    sealed interface InProgress : DownloadStatusModel {
        val progress: Float

        val progressStr: String
            get() = formatDownloadProgress(progress)

        data class Downloading(
            override val progress: Float,
        ) : InProgress

        data class Paused(
            override val progress: Float,
        ) : InProgress
    }

    data object Downloaded : DownloadStatusModel
}
