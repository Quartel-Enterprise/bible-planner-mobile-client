package com.quare.bibleplanner.core.sync.domain.usecase

fun interface ClearAllSyncedLocalData {
    suspend operator fun invoke()
}
