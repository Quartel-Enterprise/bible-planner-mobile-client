package com.quare.bibleplanner.feature.inappupdate.di

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.quare.bibleplanner.feature.inappupdate.AndroidInAppUpdater
import com.quare.bibleplanner.feature.inappupdate.InAppUpdateActivityProvider
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CompleteUpdateInstall
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.ObserveUpdateDownloadState
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.StartUpdate
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual val platformInAppUpdateModule: Module = module {
    single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
    singleOf(::InAppUpdateActivityProvider)
    singleOf(::AndroidInAppUpdater)
    factory<CheckForUpdate> {
        val updater = get<AndroidInAppUpdater>()
        CheckForUpdate { updater.check() }
    }
    factory<StartUpdate> {
        val updater = get<AndroidInAppUpdater>()
        StartUpdate { updater.start() }
    }
    factory<ObserveUpdateDownloadState> {
        val updater = get<AndroidInAppUpdater>()
        ObserveUpdateDownloadState { updater.downloadState }
    }
    factory<CompleteUpdateInstall> {
        val updater = get<AndroidInAppUpdater>()
        CompleteUpdateInstall { updater.complete() }
    }
}
