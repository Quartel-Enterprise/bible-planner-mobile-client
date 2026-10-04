package com.quare.bibleplanner.tools.agentcli.log

import co.touchlab.kermit.Severity
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.TimeSource

internal class SessionLogWriterTest {
    private val log = SessionLog(TimeSource.Monotonic)
    private val writer = SessionLogWriter(log)

    @Test
    fun `an agent only sees warnings and errors`() {
        // When
        val isInfoLoggable = writer.isLoggable(
            tag = "Sync",
            severity = Severity.Info,
        )
        val isWarnLoggable = writer.isLoggable(
            tag = "Sync",
            severity = Severity.Warn,
        )

        // Then
        assertFalse(isInfoLoggable)
        assertTrue(isWarnLoggable)
    }

    @Test
    fun `logs the message with its error`() {
        // When
        writer.log(
            severity = Severity.Error,
            message = "sync failed",
            tag = "Sync",
            throwable = IllegalStateException("offline"),
        )

        // Then
        assertEquals(
            expected = listOf(
                LogEntry(
                    kind = LogKind.LOG,
                    source = "Sync",
                    payload = Json.parseToJsonElement(
                        """{"severity": "Error", "message": "sync failed", "error": "java.lang.IllegalStateException: offline"}""",
                    ),
                ),
            ),
            actual = log.takeEntries(),
        )
    }
}
