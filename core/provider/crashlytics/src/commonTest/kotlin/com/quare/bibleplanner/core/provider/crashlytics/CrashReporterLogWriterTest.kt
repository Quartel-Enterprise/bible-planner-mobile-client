package com.quare.bibleplanner.core.provider.crashlytics

import co.touchlab.kermit.Severity
import com.quare.bibleplanner.core.provider.crashlytics.fake.RecordingCrashReporter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CrashReporterLogWriterTest {
    private lateinit var crashReporter: RecordingCrashReporter
    private lateinit var logWriter: CrashReporterLogWriter

    @Test
    fun `GIVEN every severity WHEN checking what is loggable THEN only errors and above are reported`() {
        // When
        val loggable = Severity.entries.filter { severity ->
            logWriter.isLoggable(
                tag = TAG,
                severity = severity,
            )
        }

        // Then
        assertEquals(listOf(Severity.Error, Severity.Assert), loggable)
    }

    @Test
    fun `GIVEN an error log with a throwable WHEN writing it THEN records the throwable as a non-fatal`() {
        // Given
        val throwable = IllegalStateException("boom")

        // When
        logWriter.log(
            severity = Severity.Error,
            message = "Something failed",
            tag = TAG,
            throwable = throwable,
        )

        // Then
        assertEquals(listOf<Throwable>(throwable), crashReporter.recordedExceptions)
    }

    @Test
    fun `GIVEN an error log without a throwable WHEN writing it THEN records nothing`() {
        // When
        logWriter.log(
            severity = Severity.Error,
            message = "Something failed",
            tag = TAG,
            throwable = null,
        )

        // Then
        assertTrue(crashReporter.recordedExceptions.isEmpty())
    }

    @BeforeTest
    fun setUp() {
        crashReporter = RecordingCrashReporter()
        logWriter = CrashReporterLogWriter(crashReporter)
    }

    private companion object {
        const val TAG = "Sync"
    }
}
