package com.quare.bibleplanner.core.network.data.utils

import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class HttpClientTest {
    @Test
    fun `GIVEN the app http client WHEN creating it THEN installs the timeout and json content negotiation`() {
        // When
        val client = getHttpClient()

        // Then
        assertNotNull(client.plugin(HttpTimeout))
        assertNotNull(client.plugin(ContentNegotiation))
        client.close()
    }

    @Test
    fun `GIVEN the app http client WHEN sending a request THEN targets the api host and speaks json`() = runTest {
        // Given
        val client = getHttpClient()
        lateinit var sentRequest: HttpRequestBuilder
        client.sendPipeline.intercept(HttpSendPipeline.Before) {
            sentRequest = context
            throw RequestCapturedException()
        }

        // When
        assertFailsWith<RequestCapturedException> { client.get("releases") }

        // Then
        assertEquals("api.borarachar.com", sentRequest.url.host)
        assertEquals(ContentType.Application.Json.toString(), sentRequest.headers[HttpHeaders.Accept])
        assertEquals(ContentType.Application.Json.toString(), sentRequest.headers[HttpHeaders.ContentType])
        client.close()
    }
}

private class RequestCapturedException : Exception()
