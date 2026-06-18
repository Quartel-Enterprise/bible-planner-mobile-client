package com.quare.bibleplanner.core.loginnudge.domain.usecase

/**
 * Whether the login nudge should be shown right now: the user is logged out, online, has not opted
 * out permanently and is not within the snooze window.
 */
fun interface ShouldShowLoginNudge {
    suspend operator fun invoke(): Boolean
}
