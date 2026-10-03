package com.quare.bibleplanner.core.clear.domain

// Why: study access is entitlement-gated per user, so the cache must not survive a logout or
// another account on the device would inherit studies it never unlocked.
fun interface ClearDayStudyLocalData {
    suspend operator fun invoke()
}
