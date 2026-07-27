package com.quare.bibleplanner.core.provider.billing.data.datasource

import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://api.revenuecat.com"
private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val PLATFORM_HEADER = "X-Platform"
private const val PLATFORM_WEB = "web"

internal fun createRevenueCatHttpClient(config: DesktopBillingConfig): HttpClient = HttpClient(CIO) {
    defaultRequest {
        url(BASE_URL)
        accept(ContentType.Application.Json)
        bearerAuth(config.apiKey)
        header(PLATFORM_HEADER, PLATFORM_WEB)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
    install(ContentNegotiation) {
        json(
            json = Json {
                isLenient = true
                ignoreUnknownKeys = true
            },
            contentType = ContentType.Application.Json,
        )
    }
}
