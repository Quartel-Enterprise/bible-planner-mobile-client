package com.quare.bibleplanner.core.network.data.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://api.borarachar.com"
private const val REQUEST_TIMEOUT_MILLIS = 10_000L

internal fun getHttpClient(): HttpClient = HttpClient {
    defaultRequest {
        url(BASE_URL)
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                explicitNulls = false
                ignoreUnknownKeys = true
            },
        )
    }
}
