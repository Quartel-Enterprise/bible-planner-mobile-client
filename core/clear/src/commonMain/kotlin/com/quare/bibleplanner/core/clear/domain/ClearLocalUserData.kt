package com.quare.bibleplanner.core.clear.domain

/**
 * Clears all the signed-in user's local data so a different account on the same device never
 * inherits it. Aggregates the per-domain clears (reading data in the database, reading-plan
 * preferences in DataStore). Used on logout (and any future account-data reset).
 */
fun interface ClearLocalUserData {
    suspend operator fun invoke()
}
