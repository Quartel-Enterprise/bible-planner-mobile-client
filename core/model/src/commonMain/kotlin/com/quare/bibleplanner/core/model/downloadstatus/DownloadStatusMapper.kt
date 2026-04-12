package com.quare.bibleplanner.core.model.downloadstatus

class DownloadStatusMapper {
    fun map(
        status: DownloadStatus,
        progress: Float,
    ): DownloadStatusModel = when (status) {
        DownloadStatus.DONE -> DownloadStatusModel.Downloaded
        DownloadStatus.IN_PROGRESS -> if (progress >= 1f) DownloadStatusModel.Downloaded else DownloadStatusModel.InProgress.Downloading(progress)
        DownloadStatus.PAUSED -> DownloadStatusModel.InProgress.Paused(progress)
        DownloadStatus.NOT_STARTED -> DownloadStatusModel.NotStarted
    }
}
