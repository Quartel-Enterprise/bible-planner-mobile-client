package com.quare.bibleplanner.feature.inappupdate.di

import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CompleteUpdateInstall
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.ObserveUpdateDownloadState
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.StartUpdate
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformInAppUpdateModule: Module = module {
    factory<CheckForUpdate> { CheckForUpdate { UpdateAvailability.NotAvailable } }
    factory<StartUpdate> { StartUpdate { } }
    factory<ObserveUpdateDownloadState> { ObserveUpdateDownloadState { emptyFlow() } }
    factory<CompleteUpdateInstall> { CompleteUpdateInstall { } }
}
