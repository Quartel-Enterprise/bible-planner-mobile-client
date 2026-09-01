package com.quare.bibleplanner.core.di

import com.quare.bibleplanner.core.model.AppForegroundStateHolder
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val modelModule = module {
    factoryOf(::DownloadStatusMapper)
    singleOf(::Navigator)
    singleOf(::AppForegroundStateHolder)
}
