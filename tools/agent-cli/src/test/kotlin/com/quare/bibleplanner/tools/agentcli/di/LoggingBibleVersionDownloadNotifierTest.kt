package com.quare.bibleplanner.tools.agentcli.di

import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.TimeSource

internal class LoggingBibleVersionDownloadNotifierTest {
    private val log = SessionLog(TimeSource.Monotonic)
    private val notifier = LoggingBibleVersionDownloadNotifier(log)

    @Test
    fun `GIVEN a download WHEN it progresses and finishes THEN logs each step but not every tick`() = runTest {
        // Given
        val versionId = "WEB"
        val versionName = "World English Bible"

        // When
        notifier.showProgress(versionId, versionName, 0f)
        notifier.showProgress(versionId, versionName, 0.5f)
        notifier.showPaused(versionId, versionName, 0.5f)
        notifier.showError(versionId, versionName)
        notifier.showComplete(versionId, versionName)
        notifier.dismiss(versionId)

        // Then
        assertEquals(
            expected = listOf("started", "paused", "error", "complete", "dismissed"),
            actual = log.takeEntries().map { entry ->
                entry.payload.jsonObject
                    .getValue("status")
                    .jsonPrimitive.content
            },
        )
    }
}
