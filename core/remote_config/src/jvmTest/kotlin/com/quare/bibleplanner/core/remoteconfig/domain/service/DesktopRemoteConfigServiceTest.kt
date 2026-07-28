package com.quare.bibleplanner.core.remoteconfig.domain.service

import com.quare.bibleplanner.core.remoteconfig.data.datasource.RemoteConfigProxyClient
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class DesktopRemoteConfigServiceTest {
    private val refreshInterval = 15.minutes
    private lateinit var service: RemoteConfigDataSource

    @Test
    fun `GIVEN a fetched parameter WHEN reading it as a boolean THEN returns the remote value`() = runTest {
        // Given
        prepareScenario(mapOf(SHOW_DONATE_KEY to "false"))

        // When
        val value = service.getBoolean(SHOW_DONATE_KEY)

        // Then
        assertEquals(expected = false, actual = value)
    }

    @Test
    fun `GIVEN a truthy string WHEN reading it as a boolean THEN parses it like the Firebase SDK`() = runTest {
        // Given
        prepareScenario(mapOf(SHOW_DONATE_KEY to "Yes"))

        // When
        val value = service.getBoolean(SHOW_DONATE_KEY)

        // Then
        assertEquals(expected = true, actual = value)
    }

    @Test
    fun `GIVEN a non-boolean value WHEN reading it as a boolean THEN returns null`() = runTest {
        // Given
        prepareScenario(mapOf(SHOW_DONATE_KEY to "maybe"))

        // When
        val value = service.getBoolean(SHOW_DONATE_KEY)

        // Then
        assertNull(value)
    }

    @Test
    fun `GIVEN a numeric parameter WHEN reading it as an int THEN returns the remote value`() = runTest {
        // Given
        prepareScenario(mapOf(MAX_FREE_NOTES_KEY to "7"))

        // When
        val value = service.getInt(MAX_FREE_NOTES_KEY)

        // Then
        assertEquals(expected = 7, actual = value)
    }

    @Test
    fun `GIVEN a key the template does not carry WHEN reading it THEN returns null`() = runTest {
        // Given
        prepareScenario(mapOf(SHOW_DONATE_KEY to "true"))

        // When
        val value = service.getString(MAX_FREE_NOTES_KEY)

        // Then
        assertNull(value)
    }

    @Test
    fun `GIVEN the proxy is unreachable WHEN reading a parameter THEN returns null`() = runTest {
        // Given
        prepareScenario(null)

        // When
        val value = service.getString(SHOW_DONATE_KEY)

        // Then
        assertNull(value)
    }

    @Test
    fun `GIVEN a registered listener WHEN a refresh changes a value THEN notifies it once`() = runTest {
        // Given
        prepareScenario(
            mapOf(SHOW_DONATE_KEY to "true"),
            mapOf(SHOW_DONATE_KEY to "false"),
        )
        service.getString(SHOW_DONATE_KEY)
        var updates = 0
        service.addConfigUpdateListener { updates++ }
        runCurrent()

        // When
        advanceTimeBy(refreshInterval + 1.seconds)
        runCurrent()

        // Then
        assertEquals(expected = 1, actual = updates)
        assertEquals(expected = false, actual = service.getBoolean(SHOW_DONATE_KEY))
    }

    @Test
    fun `GIVEN a cancelled listener WHEN a refresh changes a value THEN stops notifying it`() = runTest {
        // Given
        prepareScenario(
            mapOf(SHOW_DONATE_KEY to "true"),
            mapOf(SHOW_DONATE_KEY to "false"),
        )
        service.getString(SHOW_DONATE_KEY)
        var updates = 0
        val cancellable = service.addConfigUpdateListener { updates++ }
        runCurrent()

        // When
        cancellable.cancel()
        advanceTimeBy(refreshInterval + 1.seconds)
        runCurrent()

        // Then
        assertEquals(expected = 0, actual = updates)
    }

    private fun TestScope.prepareScenario(vararg responses: Map<String, String>?) {
        service = DesktopRemoteConfigService(
            proxyClient = proxyClientOf(*responses),
            coroutineScope = backgroundScope,
        )
    }

    private companion object {
        const val SHOW_DONATE_KEY = "show_donate"
        const val MAX_FREE_NOTES_KEY = "max_free_notes"
    }
}

private fun proxyClientOf(vararg responses: Map<String, String>?): RemoteConfigProxyClient {
    var index = 0
    return RemoteConfigProxyClient { responses[minOf(index++, responses.lastIndex)] }
}
