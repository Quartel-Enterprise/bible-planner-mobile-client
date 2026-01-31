package com.quare.bibleplanner.feature.bibleversion.domain.model

data class BibleVersion(
    val id: String,
    val name: String,
    val status: DownloadStatus,
)

sealed interface DownloadStatus {
    data object NotDownloaded : DownloadStatus

    data class Downloading(
        val progress: Float,
    ) : DownloadStatus

    data class Paused(
        val progress: Float,
    ) : DownloadStatus

    data object Downloaded : DownloadStatus
}
