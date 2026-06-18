package com.quare.bibleplanner.core.loginnudge.domain.usecase

/** Opts the user out of the login nudge for good (the "Don't show again" choice). */
fun interface DismissLoginNudgePermanently {
    suspend operator fun invoke()
}
