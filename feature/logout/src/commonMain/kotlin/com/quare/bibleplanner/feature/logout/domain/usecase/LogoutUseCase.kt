package com.quare.bibleplanner.feature.logout.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Signs the user out and wipes their local data so a different account on the same device never
 * inherits it.
 *
 * Order matters:
 *  1. Flush pending changes ([FlushPendingChangesUseCase]) while still authenticated, unless
 *     [shouldFlushPending] is false (the user chose to sign out anyway after a prior flush failure).
 *     If the flush fails, logout is aborted and the failure is returned, so unsynced changes are not
 *     silently lost — nothing else is torn down and the user stays logged in to retry or sign out anyway.
 *  2. [EndSession] tears down realtime, signs out, and clears local data.
 *
 * Emits a [LogoutProgress.InProgress] before each phase, then exactly one [LogoutProgress.Finished]
 * with a failure if the flush fails (logout aborted) or if sign-out/clear fails.
 */
class LogoutUseCase(
    private val flushPendingChanges: FlushPendingChangesUseCase,
    private val endSession: EndSession,
) : Logout {
    override fun invoke(shouldFlushPending: Boolean): Flow<LogoutProgress> = flow {
        if (shouldFlushPending) {
            emit(LogoutProgress.InProgress(LogoutPhase.SYNCING))
            val flushError = flushPendingChanges().exceptionOrNull()
            if (flushError != null) {
                emit(LogoutProgress.Finished(Result.failure(LogoutFlushFailedException(flushError))))
                return@flow
            }
        }
        emit(LogoutProgress.InProgress(LogoutPhase.ENDING_SESSION))
        emit(LogoutProgress.Finished(endSession()))
    }
}
