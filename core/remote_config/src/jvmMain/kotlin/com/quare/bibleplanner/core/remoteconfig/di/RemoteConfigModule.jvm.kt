package com.quare.bibleplanner.core.remoteconfig.di

import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val remoteConfigModule: Module = module {
    single {
        object : RemoteConfigService {
            override suspend fun getBoolean(key: String): Boolean = true

            override suspend fun getInt(key: String): Int = 0
        }
    }
}
