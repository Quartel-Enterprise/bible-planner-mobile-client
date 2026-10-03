package com.quare.bibleplanner.feature.logout.domain.usecase

// Why: runs only after pending changes are flushed; if sign-out fails (e.g. offline) local data is
// kept and the user stays logged in.
fun interface EndSession {
    suspend operator fun invoke(): Result<Unit>
}
