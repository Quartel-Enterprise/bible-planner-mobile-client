package com.quare.bibleplanner.core.provider.analytics

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MeasurementProtocolClientTest {
    private val clientIdProvider = ClientIdProvider()
    private lateinit var client: MeasurementProtocolClient
    private lateinit var requests: MutableList<HttpRequestData>
    private lateinit var bodies: MutableList<String>

    @Test
    fun `GIVEN configured credentials WHEN sending THEN posts to the collect endpoint with them`() = runTest {
        // Given
        prepareScenario()

        // When
        send()

        // Then
        val request = requests.single()
        assertEquals(
            expected = "https://www.google-analytics.com/mp/collect?measurement_id=G-TEST&api_secret=secret",
            actual = request.url.toString(),
        )
    }

    @Test
    fun `GIVEN an event WHEN sending THEN builds the payload with the client id user properties and typed params`() =
        runTest {
            // Given
            prepareScenario()

            // When
            send()

            // Then
            assertEquals(
                expected = JsonObject(
                    mapOf(
                        "client_id" to JsonPrimitive(clientIdProvider.getClientId()),
                        "user_properties" to JsonObject(
                            mapOf(
                                "platform" to userProperty("desktop"),
                                "is_tester" to userProperty("true"),
                            ),
                        ),
                        "events" to JsonArray(
                            listOf(
                                JsonObject(
                                    mapOf(
                                        "name" to JsonPrimitive("chapter_read"),
                                        "params" to JsonObject(
                                            mapOf(
                                                "chapter" to JsonPrimitive(3L),
                                                "progress" to JsonPrimitive(0.5),
                                                "book" to JsonPrimitive("GEN"),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                actual = Json.parseToJsonElement(bodies.single()),
            )
        }

    @Test
    fun `GIVEN missing credentials WHEN sending THEN sends nothing`() = runTest {
        // Given
        prepareScenario(measurementId = "")

        // When
        send()

        // Then
        assertTrue(requests.isEmpty())
    }

    @Test
    fun `GIVEN a network failure WHEN sending THEN swallows it`() = runTest {
        // Given
        prepareScenario(shouldFail = true)

        // When
        send()

        // Then
        assertEquals(
            expected = 1,
            actual = requests.size,
        )
    }

    private suspend fun send() {
        client.send(
            eventName = "chapter_read",
            params = mapOf(
                "chapter" to 3L,
                "progress" to 0.5,
                "book" to "GEN",
            ),
            userProperties = mapOf(
                "is_tester" to "true",
                "plan" to null,
            ),
        )
    }

    private fun userProperty(value: String): JsonObject = JsonObject(mapOf("value" to JsonPrimitive(value)))

    private fun prepareScenario(
        measurementId: String = "G-TEST",
        shouldFail: Boolean = false,
    ) {
        requests = mutableListOf()
        bodies = mutableListOf()
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                addHandler { request ->
                    requests += request
                    bodies += request.body.toByteArray().decodeToString()
                    if (shouldFail) error("network down")
                    respondOk()
                }
            },
        )
        client = MeasurementProtocolClient(
            httpClient = HttpClient(engine),
            clientIdProvider = clientIdProvider,
            measurementId = measurementId,
            apiSecret = "secret",
        )
    }
}
