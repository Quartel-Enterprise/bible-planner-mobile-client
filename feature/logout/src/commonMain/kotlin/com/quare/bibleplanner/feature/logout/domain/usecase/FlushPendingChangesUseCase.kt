package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.sync.domain.usecase.PushAllPending
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

/**
 * Flushes pending local changes to the backend before logout clears them, bounded by [flushTimeout].
 * Returns a failed [Result] if the push fails or does not complete within [flushTimeout]. Covers every
 * synced dataset via [PushAllPending], so new datasets are flushed automatically.
 */
class FlushPendingChangesUseCase(
    private val pushAllPending: PushAllPending,
    private val flushTimeout: Duration,
) {
    suspend operator fun invoke(): Result<Unit> = suspendRunCatching {
        withTimeoutOrNull(flushTimeout) {
            pushAllPending()
        } ?: throw FlushTimeoutException(flushTimeout)
    }
}
