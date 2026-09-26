package com.quare.bibleplanner.core.provider.analytics

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class DesktopAnalyticsServiceTest {
    private val sendTimeout = 5.seconds
    private lateinit var service: DesktopAnalyticsService
    private lateinit var sentPayloads: Channel<JsonObject>

    @BeforeTest
    fun setUp() {
        sentPayloads = Channel(Channel.UNLIMITED)
        val engine = MockEngine(
            MockEngineConfig().apply {
                addHandler { request ->
                    sentPayloads.send(Json.parseToJsonElement(request.body.toByteArray().decodeToString()).jsonObject)
                    respondOk()
                }
            },
        )
        service = DesktopAnalyticsService(
            MeasurementProtocolClient(
                httpClient = HttpClient(engine),
                clientIdProvider = ClientIdProvider(),
                measurementId = "G-TEST",
                apiSecret = "secret",
            ),
        )
    }

    @Test
    fun `GIVEN an event WHEN logging it THEN sends it tagged with the session and engagement params`() = runTest {
        // When
        service.logEvent(
            name = "chapter_read",
            params = mapOf("book" to "GEN"),
        )

        // Then
        val event = nextEvent()
        assertEquals(
            expected = "chapter_read",
            actual = event.getValue("name").jsonPrimitive.content,
        )
        val params = event.getValue("params").jsonObject
        assertEquals(
            expected = "GEN",
            actual = params.getValue("book").jsonPrimitive.content,
        )
        assertEquals(
            expected = "1",
            actual = params.getValue("engagement_time_msec").jsonPrimitive.content,
        )
        assertNotNull(params["session_id"])
    }

    @Test
    fun `GIVEN a user property WHEN setting it THEN sends a session start carrying it`() = runTest {
        // When
        service.setUserProperty(
            name = "is_tester",
            value = "true",
        )

        // Then
        val payload = nextPayload()
        assertEquals(
            expected = "desktop_session_start",
            actual = payload
                .firstEvent()
                .getValue("name")
                .jsonPrimitive.content,
        )
        assertEquals(
            expected = "true",
            actual = payload
                .getValue("user_properties")
                .jsonObject
                .getValue("is_tester")
                .jsonObject
                .getValue("value")
                .jsonPrimitive
                .content,
        )
    }

    @Test
    fun `GIVEN the desktop app WHEN reading the app instance id THEN there is none`() = runTest {
        // When
        val appInstanceId = service.getAppInstanceId()

        // Then
        assertNull(appInstanceId)
    }

    private suspend fun nextPayload(): JsonObject = withContext(Dispatchers.Default) {
        withTimeout(sendTimeout) { sentPayloads.receive() }
    }

    private suspend fun nextEvent(): JsonObject = nextPayload().firstEvent()

    private fun JsonObject.firstEvent(): JsonObject = getValue("events")
        .jsonArray
        .first()
        .jsonObject
}
