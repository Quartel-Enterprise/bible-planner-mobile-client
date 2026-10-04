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
    fun `GIVEN the session log writer WHEN checking severities THEN only warnings and errors are logged`() {
        // Given
        val tag = "Sync"

        // When
        val isInfoLoggable = writer.isLoggable(
            tag = tag,
            severity = Severity.Info,
        )
        val isWarnLoggable = writer.isLoggable(
            tag = tag,
            severity = Severity.Warn,
        )

        // Then
        assertFalse(isInfoLoggable)
        assertTrue(isWarnLoggable)
    }

    @Test
    fun `GIVEN an error WHEN logging it THEN records the message with its error`() {
        // Given
        val error = IllegalStateException("offline")

        // When
        writer.log(
            severity = Severity.Error,
            message = "sync failed",
            tag = "Sync",
            throwable = error,
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
