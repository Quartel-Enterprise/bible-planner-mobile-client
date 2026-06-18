package com.quare.bibleplanner.core.loginnudge.domain.usecase

/**
 * Single entry point the action triggers call after their optimistic local write: shows the login
 * nudge dialog when [ShouldShowLoginNudge] allows it, otherwise does nothing.
 */
fun interface RequestLoginNudgeIfNeeded {
    suspend operator fun invoke()
}
