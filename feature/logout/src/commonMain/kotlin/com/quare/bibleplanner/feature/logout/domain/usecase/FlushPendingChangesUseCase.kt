package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.sync.domain.usecase.PushAllPending
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

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
