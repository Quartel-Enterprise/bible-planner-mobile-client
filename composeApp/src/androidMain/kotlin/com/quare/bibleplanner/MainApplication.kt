package com.quare.bibleplanner

import android.app.Application
import com.quare.bibleplanner.core.provider.koin.commonKoinInitializer
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val context = this@MainApplication
        val androidModule = module {
            single { getDatabaseBuilder(context) }
        }
        initializeKoin(
            config = {
                androidContext(context)
            },
            platformModules = listOf(androidModule),
        )
    }
}
