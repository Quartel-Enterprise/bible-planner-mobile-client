package com.quare.bibleplanner.feature.logout.domain.usecase

/**
 * Signs the user out and clears their local data. When [shouldFlushPending] is false the pending
 * changes are not flushed first (used to force sign-out after a prior flush failure).
 *
 * Returns failure if the flush fails (logout aborted) or if sign-out/clear fails.
 */
fun interface Logout {
    suspend operator fun invoke(shouldFlushPending: Boolean): Result<Unit>
}
