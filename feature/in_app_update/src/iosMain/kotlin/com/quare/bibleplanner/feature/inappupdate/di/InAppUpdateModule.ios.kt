package com.quare.bibleplanner.feature.inappupdate.di

import com.quare.bibleplanner.feature.inappupdate.IosCheckForUpdate
import com.quare.bibleplanner.feature.inappupdate.IosStartUpdate
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CompleteUpdateInstall
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.ObserveUpdateDownloadState
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.StartUpdate
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformInAppUpdateModule: Module = module {
    factoryOf(::IosCheckForUpdate).bind<CheckForUpdate>()
    factoryOf(::IosStartUpdate).bind<StartUpdate>()
    factory<ObserveUpdateDownloadState> { ObserveUpdateDownloadState { emptyFlow() } }
    factory<CompleteUpdateInstall> { CompleteUpdateInstall { } }
}
