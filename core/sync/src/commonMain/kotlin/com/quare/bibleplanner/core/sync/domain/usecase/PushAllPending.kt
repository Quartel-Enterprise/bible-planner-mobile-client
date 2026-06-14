package com.quare.bibleplanner.core.sync.domain.usecase

/**
 * Uploads any pending local changes across every registered dataset once. Used as a best-effort
 * flush before logout so changes made offline since the last successful sync are not lost when local
 * data is cleared. Does nothing when no user is authenticated.
 */
fun interface PushAllPending {
    suspend operator fun invoke()
}
