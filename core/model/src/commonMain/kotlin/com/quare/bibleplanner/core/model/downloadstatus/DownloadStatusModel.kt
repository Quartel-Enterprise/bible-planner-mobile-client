package com.quare.bibleplanner.core.model.downloadstatus

import kotlin.math.roundToInt

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
         * The formatted progress string for display (e.g. "90", "90.25").
         */
        val progressStr: String
            get() {
                val rounded = (progress * 10000).roundToInt()
                val intPart = rounded / 100
                val fracPart = rounded % 100
                return if (fracPart == 0) "$intPart" else "$intPart.${fracPart.toString().padStart(2, '0')}"
            }

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
