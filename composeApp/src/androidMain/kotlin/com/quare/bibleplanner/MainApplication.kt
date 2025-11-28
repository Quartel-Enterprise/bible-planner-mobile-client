package com.quare.bibleplanner

import android.app.Application
import com.quare.bibleplanner.core.books.data.datasource.AndroidResourceReader
import com.quare.bibleplanner.core.books.data.datasource.ResourceReader
import com.quare.bibleplanner.core.provider.koin.initializeKoin
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val context = this@MainApplication
        val androidModule = module {
            single { getDatabaseBuilder(context) }
            single<ResourceReader> { AndroidResourceReader(context) }
        }
        initializeKoin(
            config = {
                androidContext(context)
            },
            platformModules = listOf(androidModule),
        )
    }
}
