package com.quare.bibleplanner.feature.logout.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
