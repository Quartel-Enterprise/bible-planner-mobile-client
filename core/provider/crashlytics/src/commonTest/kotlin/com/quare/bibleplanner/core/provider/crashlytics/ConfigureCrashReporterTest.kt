package com.quare.bibleplanner.core.provider.crashlytics

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import com.quare.bibleplanner.core.provider.crashlytics.fake.RecordingCrashReporter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ConfigureCrashReporterTest {
    private lateinit var crashReporter: RecordingCrashReporter

    @BeforeTest
    fun setUp() {
        crashReporter = RecordingCrashReporter()
        Logger.setLogWriters(emptyList())
    }

    @AfterTest
    fun tearDown() {
        Logger.setLogWriters(platformLogWriter())
    }

    @Test
    fun `GIVEN a release build WHEN configuring THEN enables collection and reports logged errors`() {
        // Given
        val throwable = IllegalStateException("boom")

        // When
        crashReporter.configure(isDebug = false)
        Logger.e(throwable) { "Something failed" }

        // Then
        assertEquals(listOf(true), crashReporter.collectionEnabledValues)
        assertEquals(listOf<Throwable>(throwable), crashReporter.recordedExceptions)
    }

    @Test
    fun `GIVEN a debug build WHEN configuring THEN disables collection and reports no logged errors`() {
        // When
        crashReporter.configure(isDebug = true)
        Logger.e(IllegalStateException("boom")) { "Something failed" }

        // Then
        assertEquals(listOf(false), crashReporter.collectionEnabledValues)
        assertTrue(crashReporter.recordedExceptions.isEmpty())
    }
}
