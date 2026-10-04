package com.quare.bibleplanner.core.provider.supabase

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Dispatcher

private const val MAX_REQUESTS_PER_HOST = 32

/*
 * Why: OkHttp queues past 5 calls per host, capping the Bible download far below what the
 * Supabase Storage CDN serves. The ceiling sits above the download's own semaphore so other
 * Supabase calls don't queue behind a download burst.
 */
internal actual fun createPlatformHttpEngine(): HttpClientEngine = OkHttp.create {
    config {
        dispatcher(
            Dispatcher().apply {
                maxRequestsPerHost = MAX_REQUESTS_PER_HOST
            },
        )
    }
}
