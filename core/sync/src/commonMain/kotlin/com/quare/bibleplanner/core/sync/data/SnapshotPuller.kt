package com.quare.bibleplanner.core.sync.data

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import com.quare.bibleplanner.core.utils.suspendRunCatching

internal class SnapshotPuller(
    private val synchronizers: List<Synchronizer>,
    private val trackEvent: TrackEvent,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend fun pullAll() {
        var hasFailure = false
        synchronizers.forEach { synchronizer ->
            suspendRunCatching { synchronizer.pullSnapshot() }
                .onFailure { error ->
                    hasFailure = true
                    logger.e(error) { "Failed to pull snapshot" }
                }
        }
        trackEvent(
            name = if (hasFailure) AnalyticsEventNames.SYNC_FAILED else AnalyticsEventNames.SYNC_COMPLETED,
            params = emptyMap(),
        )
    }

    private companion object {
        const val LOG_TAG = "Sync"
    }
}
