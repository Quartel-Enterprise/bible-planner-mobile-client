package com.quare.bibleplanner.core.provider.billing.data.datasource

import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RevenueCatHttpClientTest {
    private lateinit var httpClient: HttpClient
    private lateinit var sentRequests: MutableList<HttpRequestBuilder>

    @BeforeTest
    fun setUp() {
        sentRequests = mutableListOf()
        httpClient = createRevenueCatHttpClient(
            DesktopBillingConfig(
                apiKey = "rc-web-key",
                purchaseLink = "",
            ),
        )
        httpClient.sendPipeline.intercept(HttpSendPipeline.Engine) {
            sentRequests += context
            throw RequestIntercepted()
        }
    }

    @AfterTest
    fun tearDown() {
        httpClient.close()
    }

    @Test
    fun `GIVEN a relative path WHEN requesting THEN targets the RevenueCat api authenticated as the web platform`() =
        runTest {
            // When
            assertFailsWith<RequestIntercepted> { httpClient.get("v1/subscribers/user-1") }

            // Then
            val request = sentRequests.single()
            assertEquals(
                expected = "https://api.revenuecat.com/v1/subscribers/user-1",
                actual = request.url.buildString(),
            )
            assertEquals(
                expected = "Bearer rc-web-key",
                actual = request.headers[HttpHeaders.Authorization],
            )
            assertEquals(
                expected = "web",
                actual = request.headers["X-Platform"],
            )
            assertEquals(
                expected = "application/json",
                actual = request.headers[HttpHeaders.Accept],
            )
        }
}

private class RequestIntercepted : RuntimeException()
