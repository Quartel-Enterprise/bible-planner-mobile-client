package com.quare.bibleplanner.e2e.harness

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier

internal class SilentBibleVersionDownloadNotifier : BibleVersionDownloadNotifier {
    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {}

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {}

    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {}

    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {}

    override suspend fun dismiss(versionId: String) {}
}
