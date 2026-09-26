package com.quare.bibleplanner.core.provider.supabase

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import io.github.jan.supabase.logging.LogLevel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class KermitSupabaseLoggingProcessorTest {
    private lateinit var processor: KermitSupabaseLoggingProcessor
    private lateinit var logWriter: RecordingLogWriter

    @BeforeTest
    fun setUp() {
        logWriter = RecordingLogWriter()
        Logger.setLogWriters(logWriter)
        Logger.setMinSeverity(Severity.Verbose)
    }

    @AfterTest
    fun tearDown() {
        Logger.setLogWriters(platformLogWriter())
    }

    @Test
    fun `GIVEN a warning threshold WHEN checking each level THEN enables only warnings and above`() {
        // Given
        prepareScenario(minLevel = LogLevel.WARNING)

        // When
        val enabled = LogLevel.entries.filter(processor::isEnabled)

        // Then
        assertEquals(listOf(LogLevel.WARNING, LogLevel.ERROR, LogLevel.NONE), enabled)
    }

    @Test
    fun `GIVEN each supabase level WHEN logging THEN writes it with the matching kermit severity and tag`() {
        // Given
        prepareScenario(minLevel = LogLevel.DEBUG)

        // When
        listOf(LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARNING, LogLevel.NONE).forEach { level ->
            log(
                level = level,
                throwable = null,
            )
        }

        // Then
        assertEquals(
            listOf(Severity.Debug, Severity.Info, Severity.Warn),
            logWriter.entries.map(LogEntry::severity),
        )
        assertTrue(logWriter.entries.all { entry -> entry.tag == TAG })
    }

    @Test
    fun `GIVEN an error without a throwable WHEN logging THEN reports it with an exception carrying the message`() {
        // Given
        prepareScenario(minLevel = LogLevel.DEBUG)

        // When
        log(
            level = LogLevel.ERROR,
            throwable = null,
        )

        // Then
        val entry = logWriter.entries.single()
        assertEquals(Severity.Error, entry.severity)
        val exception = assertIs<SupabaseLogException>(entry.throwable)
        assertEquals(MESSAGE, exception.message)
    }

    @Test
    fun `GIVEN an error with a throwable WHEN logging THEN reports that throwable`() {
        // Given
        prepareScenario(minLevel = LogLevel.DEBUG)
        val throwable = IllegalArgumentException("bad request")

        // When
        log(
            level = LogLevel.ERROR,
            throwable = throwable,
        )

        // Then
        val entry = logWriter.entries.single()
        assertEquals(Severity.Error, entry.severity)
        assertEquals(throwable, entry.throwable)
    }

    @Test
    fun `GIVEN the missing stored session error WHEN logging THEN downgrades it to debug`() {
        // Given
        prepareScenario(minLevel = LogLevel.DEBUG)

        // When
        log(
            level = LogLevel.ERROR,
            throwable = IllegalStateException("No entry with the key session"),
        )

        // Then
        assertEquals(listOf(Severity.Debug), logWriter.entries.map(LogEntry::severity))
    }

    private fun log(
        level: LogLevel,
        throwable: Throwable?,
    ) {
        processor.processLog(
            level = level,
            tag = TAG,
            throwable = throwable,
            message = MESSAGE,
        )
    }

    private fun prepareScenario(minLevel: LogLevel) {
        processor = KermitSupabaseLoggingProcessor(minLevel)
    }

    private companion object {
        const val TAG = "Supabase-Auth"
        const val MESSAGE = "Refreshing session"
    }
}

private data class LogEntry(
    val severity: Severity,
    val tag: String,
    val throwable: Throwable?,
)

private class RecordingLogWriter : LogWriter() {
    val entries = mutableListOf<LogEntry>()

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        entries += LogEntry(
            severity = severity,
            tag = tag,
            throwable = throwable,
        )
    }
}
