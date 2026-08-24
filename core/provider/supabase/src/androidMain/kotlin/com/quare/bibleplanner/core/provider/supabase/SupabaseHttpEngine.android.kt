package com.quare.bibleplanner.core.provider.supabase

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Dispatcher

private const val MAX_REQUESTS_PER_HOST = 32

/**
 * OkHttp queues everything past 5 concurrent calls per host, and Ktor installs that default
 * dispatcher, so the Bible download was capped far below what Supabase Storage serves — its public
 * objects come off the CDN edge, which answers far more than this in parallel.
 *
 * The ceiling is deliberately above the download's own semaphore: the download is what decides how
 * hard to pull, and the headroom keeps the app's other Supabase calls from queueing behind a burst.
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
