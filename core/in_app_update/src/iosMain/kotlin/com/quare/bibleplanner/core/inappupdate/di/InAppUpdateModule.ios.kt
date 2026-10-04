package com.quare.bibleplanner.core.inappupdate.di

import com.quare.bibleplanner.core.inappupdate.IosStartUpdate
import com.quare.bibleplanner.core.inappupdate.data.DeviceRegionProvider
import com.quare.bibleplanner.core.inappupdate.data.ItunesCheckForUpdate
import com.quare.bibleplanner.core.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.core.inappupdate.domain.usecase.CompleteUpdateInstall
import com.quare.bibleplanner.core.inappupdate.domain.usecase.ObserveUpdateDownloadState
import com.quare.bibleplanner.core.inappupdate.domain.usecase.StartUpdate
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale

internal actual val platformInAppUpdateModule: Module = module {
    factory<DeviceRegionProvider> { DeviceRegionProvider { NSLocale.currentLocale.countryCode } }
    factoryOf(::ItunesCheckForUpdate).bind<CheckForUpdate>()
    factoryOf(::IosStartUpdate).bind<StartUpdate>()
    factory<ObserveUpdateDownloadState> { ObserveUpdateDownloadState { emptyFlow() } }
    factory<CompleteUpdateInstall> { CompleteUpdateInstall { } }
}
