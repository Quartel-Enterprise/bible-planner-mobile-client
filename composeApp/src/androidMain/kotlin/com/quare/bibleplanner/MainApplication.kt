package com.quare.bibleplanner

import android.app.Application
import com.quare.bibleplanner.core.provider.koin.initializeKoin
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val context = this@MainApplication
        initializeKoin(
            config = {
                androidContext(context)
            },
        )
    }
}
