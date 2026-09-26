package com.quare.bibleplanner.core.network.data.handler

import com.quare.bibleplanner.core.network.data.fake.FakeHttpEngine
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class RequestHandlerTest {
    private lateinit var requestHandler: RequestHandler
    private lateinit var httpClient: HttpClient

    @AfterTest
    fun tearDown() {
        httpClient.close()
    }

    @Test
    fun `GIVEN a successful response WHEN calling THEN returns the decoded body`() = runTest {
        // Given
        prepareScenario { HttpStatusCode.OK to "release notes" }

        // When
        val result = requestHandler.call<String> { get(PATH) }

        // Then
        assertEquals("release notes", result.getOrThrow())
    }

    @Test
    fun `GIVEN an error response and an error mapper WHEN calling THEN fails with the mapped error`() = runTest {
        // Given
        prepareScenario { HttpStatusCode.NotFound to "" }

        // When
        val result = requestHandler.call<String>(
            onError = { errorCode -> NoSuchElementException("missing $errorCode") },
            block = { get(PATH) },
        )

        // Then
        val error = assertIs<NoSuchElementException>(result.exceptionOrNull())
        assertEquals("missing 404", error.message)
    }

    @Test
    fun `GIVEN an error response without an error mapper WHEN calling THEN fails with the status`() = runTest {
        // Given
        prepareScenario { HttpStatusCode.InternalServerError to "" }

        // When
        val result = requestHandler.call<String> { get(PATH) }

        // Then
        assertEquals(
            "Unexpected status code: 500 Internal Server Error",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun `GIVEN a mapper that maps nothing WHEN the response fails THEN falls back to the status error`() = runTest {
        // Given
        prepareScenario { HttpStatusCode.BadGateway to "" }

        // When
        val result = requestHandler.call<String>(
            onError = { null },
            block = { get(PATH) },
        )

        // Then
        assertEquals(
            "Unexpected status code: 502 Bad Gateway",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun `GIVEN the request throws WHEN calling THEN fails with that exception`() = runTest {
        // Given
        prepareScenario { error("connection reset") }

        // When
        val result = requestHandler.call<String> { get(PATH) }

        // Then
        val error = assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals("connection reset", error.message)
    }

    private fun prepareScenario(respond: () -> Pair<HttpStatusCode, String>) {
        httpClient = HttpClient(FakeHttpEngine { respond() })
        requestHandler = RequestHandler(httpClient)
    }

    private companion object {
        const val PATH = "https://api.example.com/notes"
    }
}
