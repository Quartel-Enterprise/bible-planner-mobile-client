package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.PushPendingFavorites
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

/**
 * Flushes pending local changes to the backend before logout clears them, bounded by [flushTimeout].
 * Returns a failed [Result] if the push fails or does not complete within [flushTimeout]. New kinds
 * of pending data should be pushed here as they are added.
 */
class FlushPendingChangesUseCase(
    private val pushPendingFavorites: PushPendingFavorites,
    private val flushTimeout: Duration,
) {
    suspend operator fun invoke(): Result<Unit> = suspendRunCatching {
        withTimeoutOrNull(flushTimeout) {
            pushPendingFavorites()
        } ?: throw FlushTimeoutException(flushTimeout)
    }
}
