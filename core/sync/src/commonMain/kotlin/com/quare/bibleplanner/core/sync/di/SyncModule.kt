package com.quare.bibleplanner.core.sync.di

import com.quare.bibleplanner.core.sync.data.SnapshotPuller
import com.quare.bibleplanner.core.sync.data.SyncCoordinator
import com.quare.bibleplanner.core.sync.domain.usecase.ClearAllSyncedLocalData
import com.quare.bibleplanner.core.sync.domain.usecase.ClearAllSyncedLocalDataUseCase
import com.quare.bibleplanner.core.sync.domain.usecase.ObserveSync
import com.quare.bibleplanner.core.sync.domain.usecase.PushAllPending
import com.quare.bibleplanner.core.sync.domain.usecase.PushAllPendingUseCase
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Wires the generic sync engine. Each dataset registers its own
 * [com.quare.bibleplanner.core.sync.domain.Synchronizer] in its feature module; the engine collects
 * them all via [org.koin.core.scope.Scope.getAll].
 */
val syncModule = module {
    single<ObserveSync> {
        SyncCoordinator(
            observeAuthenticatedUserId = get(),
            synchronizers = getAll(),
            snapshotPuller = SnapshotPuller(
                synchronizers = getAll(),
                trackEvent = get(),
            ),
            realtime = get(),
            currentTimestampProvider = get(),
        )
    }
    single<PushAllPending> {
        PushAllPendingUseCase(synchronizers = getAll())
    }
    single<ClearAllSyncedLocalData> {
        ClearAllSyncedLocalDataUseCase(synchronizers = getAll())
    }
}
