package com.quare.bibleplanner.core.date

import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

internal class NetworkTimeTimestampProviderTest {
    private val serverTimestamp = 1_445_412_480_000L
    private val tolerance = 1.minutes

    private lateinit var provider: NetworkTimeTimestampProvider
    private lateinit var syncJob: Job
    private lateinit var requestedMethods: MutableList<HttpMethod>

    @Test
    fun `GIVEN the server reports its date WHEN reading the time THEN follows the server clock`() = runTest {
        // Given
        prepareScenario(dateHeader = "Wed, 21 Oct 2015 07:28:00 GMT")
        syncJob.children.forEach { it.join() }

        // When
        val timestamp = provider.getCurrentTimestamp()

        // Then
        assertTrue(timestamp - serverTimestamp in 0..tolerance.inWholeMilliseconds)
        assertEquals(listOf(HttpMethod.Head), requestedMethods)
    }

    @Test
    fun `GIVEN the server sends no date WHEN reading the time THEN falls back to the device clock`() = runTest {
        // Given
        prepareScenario(dateHeader = null)
        syncJob.children.forEach { it.join() }

        // When
        val timestamp = provider.getCurrentTimestamp()

        // Then
        assertTrue(isNearDeviceClock(timestamp))
    }

    @Test
    fun `GIVEN the request fails WHEN reading the time THEN falls back to the device clock`() = runTest {
        // Given
        prepareScenario(
            dateHeader = null,
            fails = true,
        )
        syncJob.children.forEach { it.join() }

        // When
        val timestamp = provider.getCurrentTimestamp()

        // Then
        assertTrue(isNearDeviceClock(timestamp))
    }

    private fun isNearDeviceClock(timestamp: Long): Boolean =
        abs(timestamp - Clock.System.now().toEpochMilliseconds()) < tolerance.inWholeMilliseconds

    private fun prepareScenario(
        dateHeader: String?,
        fails: Boolean = false,
    ) {
        requestedMethods = mutableListOf()
        syncJob = SupervisorJob()
        val engine = MockEngine { request ->
            requestedMethods += request.method
            check(!fails) { "offline" }
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = dateHeader?.let { headersOf(HttpHeaders.Date, it) } ?: headersOf(),
            )
        }
        provider = NetworkTimeTimestampProvider(
            httpClient = HttpClient(engine),
            deviceClockTimestampProvider = DeviceClockTimestampProvider(),
            applicationScope = ApplicationScope(CoroutineScope(syncJob)),
        )
    }
}
