package com.quare.bibleplanner.core.di

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val modelModule = module {
    factoryOf(::DownloadStatusMapper)
}
