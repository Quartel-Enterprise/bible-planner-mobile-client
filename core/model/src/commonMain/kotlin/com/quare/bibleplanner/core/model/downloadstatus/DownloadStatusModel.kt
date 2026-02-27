package com.quare.bibleplanner.core.model.downloadstatus

/**
 * Represents the current download status of a Bible version.
 */
sealed interface DownloadStatusModel {
    /**
     * The download has not been initiated yet.
     */
    data object NotStarted : DownloadStatusModel

    /**
     * The download is currently in a state where progress is being tracked.
     */
    sealed interface InProgress : DownloadStatusModel {
        /**
         * The current download progress as a value between 0.0 and 1.0.
         */
        val progress: Float

        /**
         * The download is actively receiving data.
         */
        data class Downloading(
            override val progress: Float,
        ) : InProgress

        /**
         * The download has been manually paused.
         */
        data class Paused(
            override val progress: Float,
        ) : InProgress
    }

    /**
     * The download has completed successfully and all content is available locally.
     */
    data object Downloaded : DownloadStatusModel
}
