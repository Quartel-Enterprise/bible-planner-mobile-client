package com.quare.bibleplanner.core.books.domain

interface BibleVersionDownloadNotifier {
    suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    )

    suspend fun showComplete(
        versionId: String,
        versionName: String,
    )

    suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    )

    suspend fun showError(
        versionId: String,
        versionName: String,
    )

    suspend fun dismiss(versionId: String)
}
