package com.quare.bibleplanner.core.sync.domain.usecase

/**
 * Wipes the local state of every registered synced dataset, without scheduling a push. Used on
 * logout so a different account on the same device never inherits the previous user's synced data.
 */
fun interface ClearAllSyncedLocalData {
    suspend operator fun invoke()
}
