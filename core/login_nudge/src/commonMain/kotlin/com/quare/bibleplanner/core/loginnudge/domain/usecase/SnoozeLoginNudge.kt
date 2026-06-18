package com.quare.bibleplanner.core.loginnudge.domain.usecase

/** Silences the login nudge for the snooze window, starting now. */
fun interface SnoozeLoginNudge {
    suspend operator fun invoke()
}
