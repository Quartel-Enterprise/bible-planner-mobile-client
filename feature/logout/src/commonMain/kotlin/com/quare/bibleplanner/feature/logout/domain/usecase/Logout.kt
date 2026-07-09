package com.quare.bibleplanner.feature.logout.domain.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Signs the user out and clears their local data. When [shouldFlushPending] is false the pending
 * changes are not flushed first (used to force sign-out after a prior flush failure).
 *
 * Emits a [LogoutProgress.InProgress] per phase as real work happens, so the UI can reflect actual
 * progress instead of a single opaque spinner, then terminates with exactly one
 * [LogoutProgress.Finished] carrying the outcome — failure if the flush fails (logout aborted) or
 * if sign-out/clear fails.
 */
fun interface Logout {
    operator fun invoke(shouldFlushPending: Boolean): Flow<LogoutProgress>
}
