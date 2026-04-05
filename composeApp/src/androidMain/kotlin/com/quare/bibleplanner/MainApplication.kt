package com.quare.bibleplanner

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.worker.BibleInitializationWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
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
        }
        initializeKoin(
            config = {
                androidContext(context)
            },
            platformModules = listOf(androidModule),
        )

        scheduleBibleInitializationWork()
    }

    private fun scheduleBibleInitializationWork() {
        val request = OneTimeWorkRequestBuilder<BibleInitializationWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            BibleInitializationWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
