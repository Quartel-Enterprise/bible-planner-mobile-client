package com.quare.bibleplanner.core.provider.crashlytics

import io.sentry.Sentry
import io.sentry.protocol.SentryId
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DesktopCrashReporterTest {
    private lateinit var crashReporter: DesktopCrashReporter

    @AfterTest
    fun tearDown() {
        Sentry.close()
    }

    @Test
    fun `GIVEN a Sentry DSN WHEN enabling collection THEN starts Sentry tagged with the app release`() {
        // Given
        prepareScenario(sentryDsn = SENTRY_DSN)

        // When
        crashReporter.setCollectionEnabled(true)

        // Then
        assertTrue(Sentry.isEnabled())
        assertEquals("com.quare.bibleplanner@$APP_VERSION", Sentry.getCurrentScopes().options.release)
    }

    @Test
    fun `GIVEN no Sentry DSN WHEN enabling collection THEN keeps Sentry off`() {
        // Given
        prepareScenario(sentryDsn = "")

        // When
        crashReporter.setCollectionEnabled(true)

        // Then
        assertFalse(Sentry.isEnabled())
    }

    @Test
    fun `GIVEN Sentry running WHEN disabling collection THEN stops Sentry`() {
        // Given
        prepareScenario(sentryDsn = SENTRY_DSN)
        crashReporter.setCollectionEnabled(true)

        // When
        crashReporter.setCollectionEnabled(false)

        // Then
        assertFalse(Sentry.isEnabled())
    }

    @Test
    fun `GIVEN collection disabled WHEN recording an exception THEN nothing is captured`() {
        // Given
        prepareScenario(sentryDsn = SENTRY_DSN)
        crashReporter.setCollectionEnabled(false)

        // When
        crashReporter.recordException(IllegalStateException("boom"))

        // Then
        assertEquals(SentryId.EMPTY_ID, Sentry.getLastEventId())
    }

    private fun prepareScenario(sentryDsn: String) {
        crashReporter = DesktopCrashReporter(
            sentryDsn = sentryDsn,
            appVersion = APP_VERSION,
        )
    }

    private companion object {
        const val SENTRY_DSN = "http://public@127.0.0.1:9/1"
        const val APP_VERSION = "9.9.9"
    }
}
