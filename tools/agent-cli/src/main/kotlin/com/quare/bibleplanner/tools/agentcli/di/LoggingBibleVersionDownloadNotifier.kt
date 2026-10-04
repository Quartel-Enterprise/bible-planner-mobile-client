package com.quare.bibleplanner.tools.agentcli.di

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.tools.agentcli.log.LogKind
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class LoggingBibleVersionDownloadNotifier(
    private val log: SessionLog,
) : BibleVersionDownloadNotifier {
    override suspend fun showProgress(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        if (progress == 0f) {
            record(
                versionId = versionId,
                status = "started",
            )
        } else {
            log.touch()
        }
    }

    override suspend fun showComplete(
        versionId: String,
        versionName: String,
    ) {
        record(
            versionId = versionId,
            status = "complete",
        )
    }

    override suspend fun showPaused(
        versionId: String,
        versionName: String,
        progress: Float,
    ) {
        record(
            versionId = versionId,
            status = "paused",
        )
    }

    override suspend fun showError(
        versionId: String,
        versionName: String,
    ) {
        record(
            versionId = versionId,
            status = "error",
        )
    }

    override suspend fun dismiss(versionId: String) {
        record(
            versionId = versionId,
            status = "dismissed",
        )
    }

    private fun record(
        versionId: String,
        status: String,
    ) {
        log.record(
            kind = LogKind.NOTIFICATION,
            source = SOURCE,
            payload = JsonObject(
                mapOf(
                    "version" to JsonPrimitive(versionId),
                    "status" to JsonPrimitive(status),
                ),
            ),
        )
    }

    private companion object {
        const val SOURCE = "bible_version_download"
    }
}
