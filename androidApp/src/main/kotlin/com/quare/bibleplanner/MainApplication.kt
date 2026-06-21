package com.quare.bibleplanner

import android.app.Application
import android.content.pm.ApplicationInfo
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.crashlytics.configure
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import com.quare.bibleplanner.core.provider.language.di.androidLanguageProviderModule
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.supabase.di.androidSupabaseModule
import com.quare.bibleplanner.di.androidModule
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.feature.applanguage.di.androidAppLanguageModule
import com.quare.bibleplanner.feature.login.di.androidLoginModule
import com.quare.bibleplanner.notification.AndroidNotificationStringProvider
import com.quare.bibleplanner.notification.NotificationStringProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

class MainApplication : Application() {
    private val mainApplicationModule = module {
        singleOf(::AndroidNotificationStringProvider).bind<NotificationStringProvider>()
    }

    override fun onCreate() {
        super.onCreate()
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        configureRevenueCat(isDebug = isDebug)

        val context = this@MainApplication
        initializeKoin(
            config = {
                androidContext(context)
            },
            platformModules = listOf(
                androidModule,
                mainApplicationModule,
                androidAppLanguageModule,
                androidLanguageProviderModule,
                androidLoginModule,
                androidSupabaseModule,
                languageProviderModule,
            ),
        )
        KoinPlatform.getKoin().get<CrashReporter>().configure(isDebug = isDebug)
    }
}
