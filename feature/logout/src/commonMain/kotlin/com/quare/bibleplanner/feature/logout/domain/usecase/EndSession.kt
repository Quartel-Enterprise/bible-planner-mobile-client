package com.quare.bibleplanner.feature.logout.domain.usecase

/**
 * Leaves realtime channels, disconnects the socket, signs out, and clears local user data — the
 * final step of logout, run after any pending changes have been flushed.
 *
 * Returns failure if sign-out or clearing local data fails; if sign-out fails (e.g. offline) local
 * data is kept and the user stays logged in.
 */
fun interface EndSession {
    suspend operator fun invoke(): Result<Unit>
}
