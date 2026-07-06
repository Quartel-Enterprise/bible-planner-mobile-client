package com.quare.bibleplanner.core.clear.domain

/**
 * Wipes the locally cached AI day studies. Access to a study is entitlement-gated per user,
 * so the cache must not survive a logout: a different account on the same device would
 * otherwise inherit studies it never unlocked. Implemented by the day_study feature.
 */
fun interface ClearDayStudyLocalData {
    suspend operator fun invoke()
}
