package com.quare.bibleplanner.core.sync.domain.usecase

/*
 * Why: best-effort flush before logout so offline changes are not lost when local data
 * is cleared.
 */
fun interface PushAllPending {
    suspend operator fun invoke()
}
