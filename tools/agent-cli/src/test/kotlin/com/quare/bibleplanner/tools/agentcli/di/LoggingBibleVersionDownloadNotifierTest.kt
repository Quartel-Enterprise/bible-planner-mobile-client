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
        // When
        notifier.showProgress("WEB", "World English Bible", 0f)
        notifier.showProgress("WEB", "World English Bible", 0.5f)
        notifier.showPaused("WEB", "World English Bible", 0.5f)
        notifier.showError("WEB", "World English Bible")
        notifier.showComplete("WEB", "World English Bible")
        notifier.dismiss("WEB")

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
