package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.clear.domain.ClearLocalUserData
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime

/**
 * Signs the user out and wipes their local data so a different account on the same device never
 * inherits it.
 *
 * Order matters:
 *  1. Flush pending changes ([FlushPendingChangesUseCase]) while still authenticated, unless
 *     [shouldFlushPending] is false (the user chose to sign out anyway after a prior flush failure).
 *     If the flush fails, logout is aborted and the failure is returned, so unsynced changes are not
 *     silently lost — nothing else is torn down and the user stays logged in to retry or sign out anyway.
 *  2. Leave realtime channels (while still authenticated, so the unsubscribe phx_leave isn't sent
 *     during auth teardown) and disconnect the socket, so it doesn't keep reconnecting on the shared
 *     HTTP engine and stall the next login's auth request with a stale pooled connection.
 *  3. [Auth.signOut] — also stops the favorites sync pipeline, so it
 *     won't re-pull remote data into the database after we clear it.
 *  4. Clear local user data only after sign-out succeeded; if sign-out fails (e.g. offline) we keep
 *     the data and stay logged in.
 *
 * Returns failure if the flush fails (logout aborted) or if sign-out/clear fails.
 */
class LogoutUseCase(
    private val auth: Auth,
    private val realtime: Realtime,
    private val flushPendingChanges: FlushPendingChangesUseCase,
    private val clearLocalUserData: ClearLocalUserData,
) : Logout {
    override suspend fun invoke(shouldFlushPending: Boolean): Result<Unit> {
        if (shouldFlushPending) {
            flushPendingChanges().onFailure { return Result.failure(LogoutFlushFailedException(it)) }
        }
        suspendRunCatching { realtime.removeAllChannels() }
        realtime.disconnect()
        return suspendRunCatching {
            auth.signOut()
            clearLocalUserData()
        }
    }
}
