package com.quare.bibleplanner.feature.daystudy.fake

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.sse.SSECapability
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.ResponseAdapterAttributeKey
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI

internal class SseMockEngine(
    private val delegate: MockEngine,
) : HttpClientEngine by delegate {
    override val supportedCapabilities: Set<HttpClientEngineCapability<*>> =
        delegate.supportedCapabilities + SSECapability

    @OptIn(InternalAPI::class)
    override fun install(client: HttpClient) {
        super<HttpClientEngine>.install(client)
    }

    @OptIn(InternalAPI::class)
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val response = delegate.execute(data)
        val adapter = data.attributes.getOrNull(ResponseAdapterAttributeKey) ?: return response
        val body = response.body as? ByteReadChannel ?: return response
        val adapted = adapter.adapt(
            data = data,
            status = response.statusCode,
            headers = response.headers,
            responseBody = body,
            outgoingContent = data.body,
            callContext = response.callContext,
        ) ?: return response
        return HttpResponseData(
            statusCode = response.statusCode,
            requestTime = response.requestTime,
            headers = response.headers,
            version = response.version,
            body = adapted,
            callContext = response.callContext,
        )
    }
}
