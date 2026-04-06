package com.quare.bibleplanner

import android.app.Application
import android.content.pm.ApplicationInfo
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.notification.AndroidBibleVersionDownloadNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        configureRevenueCat(isDebug = isDebug)

        val context = this@MainApplication
        val androidModule = module {
            single { getDatabaseBuilder(context) }
            viewModelOf(::MainActivityViewModel)
            single { AndroidBibleVersionDownloadNotifier(androidContext()) }.bind<BibleVersionDownloadNotifier>()
        }
        initializeKoin(
            config = {
                androidContext(context)
            },
            platformModules = listOf(androidModule),
        )
    }
}
