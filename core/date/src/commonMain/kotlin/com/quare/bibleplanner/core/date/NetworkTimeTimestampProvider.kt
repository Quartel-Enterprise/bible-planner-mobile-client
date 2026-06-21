package com.quare.bibleplanner.core.date

import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.http.HttpHeaders
import io.ktor.http.fromHttpToGmtDate
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class NetworkTimeTimestampProvider(
    private val httpClient: HttpClient,
    private val deviceClockTimestampProvider: DeviceClockTimestampProvider,
    applicationScope: ApplicationScope,
) : CurrentTimestampProvider {
    @Volatile
    private var anchor: TimeAnchor? = null

    init {
        applicationScope.launch { syncWithNetworkTime() }
    }

    override fun getCurrentTimestamp(): Long = anchor?.let { (serverTimestamp, mark) ->
        serverTimestamp + mark.elapsedNow().inWholeMilliseconds
    } ?: deviceClockTimestampProvider.getCurrentTimestamp()

    private suspend fun syncWithNetworkTime() {
        suspendRunCatching {
            val serverTimestamp = httpClient
                .head(BuildKonfig.SUPABASE_URL)
                .headers[HttpHeaders.Date]
                ?.fromHttpToGmtDate()
                ?.timestamp
            val mark = TimeSource.Monotonic.markNow()
            if (serverTimestamp != null) {
                anchor = TimeAnchor(
                    serverTimestamp = serverTimestamp,
                    mark = mark,
                )
            }
        }
    }

    private data class TimeAnchor(
        val serverTimestamp: Long,
        val mark: TimeMark,
    )
}
