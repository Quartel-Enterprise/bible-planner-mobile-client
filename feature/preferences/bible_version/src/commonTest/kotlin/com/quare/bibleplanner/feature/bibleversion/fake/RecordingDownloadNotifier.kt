package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier

internal class RecordingDownloadNotifier(
    private val onCall: (String) -> Unit = {},
) : BibleVersionDownloadNotifier {
    val calls = mutableListOf<String>()

    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        record("progress $versionId $versionName $progress")
    }

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        record("complete $versionId $versionName")
    }

    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        record("paused $versionId $versionName $progress")
    }

    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {
        record("error $versionId $versionName")
    }

    override suspend fun dismiss(versionId: String) {
        record("dismiss $versionId")
    }

    private fun record(call: String) {
        synchronized(calls) { calls += call }
        onCall(call)
    }
}
