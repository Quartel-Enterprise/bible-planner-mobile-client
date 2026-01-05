package com.quare.bibleplanner.core.remoteconfig.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.quare.bibleplanner.core.remoteconfig.domain.service.AndroidRemoteConfigService
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platFormRemoteConfigModule: Module = module {
    single<FirebaseRemoteConfig> {
        val context = androidContext()
        getAndroidFirebaseRemoteConfig(context)
    }
    singleOf(::AndroidRemoteConfigService).bind<RemoteConfigService>()
}

private fun getAndroidFirebaseRemoteConfig(context: Context): FirebaseRemoteConfig =
    FirebaseRemoteConfig.getInstance().apply {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val settings = remoteConfigSettings {
            if (isDebuggable) {
                minimumFetchIntervalInSeconds = 0
            }
        }
        setConfigSettingsAsync(settings)
    }
