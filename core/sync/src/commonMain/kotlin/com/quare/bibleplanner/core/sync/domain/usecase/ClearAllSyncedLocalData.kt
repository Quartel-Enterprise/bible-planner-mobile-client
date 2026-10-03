package com.quare.bibleplanner.core.sync.domain.usecase

// Why: must not schedule a push, so wiping on logout never propagates deletions to the server
// while keeping the next account on this device from inheriting the previous user's data.
fun interface ClearAllSyncedLocalData {
    suspend operator fun invoke()
}
