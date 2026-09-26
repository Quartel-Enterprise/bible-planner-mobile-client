package com.quare.bibleplanner.core.network.data.fake

import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.callContext
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI

internal class FakeHttpEngine(
    private val respond: (HttpRequestData) -> Pair<HttpStatusCode, String>,
) : HttpClientEngineBase(ENGINE_NAME) {
    val requests = mutableListOf<HttpRequestData>()

    override val config: HttpClientEngineConfig = HttpClientEngineConfig()

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        requests += data
        val (status, body) = respond(data)
        return HttpResponseData(
            statusCode = status,
            requestTime = GMTDate(),
            headers = headersOf(),
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(body),
            callContext = callContext(),
        )
    }

    private companion object {
        const val ENGINE_NAME = "fake"
    }
}
