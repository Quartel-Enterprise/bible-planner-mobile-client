package com.quare.bibleplanner.core.provider.supabase

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Dispatcher

private const val MAX_REQUESTS_PER_HOST = 32

internal actual fun createPlatformHttpEngine(): HttpClientEngine = OkHttp.create {
    config {
        dispatcher(
            Dispatcher().apply {
                maxRequestsPerHost = MAX_REQUESTS_PER_HOST
            },
        )
    }
}
