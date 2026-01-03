package com.quare.bibleplanner

import android.app.Application
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        configureRevenueCat()

        val context = this@MainApplication
        val androidModule = module {
            single { getDatabaseBuilder(context) }
            viewModelOf(::MainActivityViewModel)
        }
        initializeKoin(
            config = {
                androidContext(context)
            },
            platformModules = listOf(androidModule),
        )
    }
}
