package com.quare.bibleplanner

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.notification.IosBibleVersionDownloadNotifier
import com.quare.bibleplanner.worker.IosBackgroundDownloadBridge
import com.quare.bibleplanner.worker.IosBibleVersionDownloaderFacade
import com.quare.bibleplanner.worker.IosDownloadSession
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi

private var isInitialized = false

/**
 * Initializes Koin early — before the UI is shown — so that the URLSession background download
 * handler can access the Koin graph even when the app is launched solely for background events.
 * Safe to call multiple times; only the first call takes effect.
 */
@OptIn(ExperimentalNativeApi::class)
fun initializeKoinForIos(
    remoteConfigService: RemoteConfigService,
    downloadSession: IosDownloadSession,
) {
    if (isInitialized) return
    try {
        initializeKoin(
            platformModules = listOf(
                module {
                    single { getDatabaseBuilder() }
                    single { remoteConfigService }
                    single { downloadSession }.bind<IosDownloadSession>()
                    single { IosBibleVersionDownloadNotifier(get()) }.bind<BibleVersionDownloadNotifier>()
                    singleOf(::IosBackgroundDownloadBridge)
                    singleOf(::IosBibleVersionDownloaderFacade).bind<com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade>()
                },
            ),
        )
        configureRevenueCat(isDebug = Platform.isDebugBinary)
        isInitialized = true
    } catch (e: Exception) {
        Logger.e(e) { "Error initializing Koin for iOS" }
    }
}

fun MainViewController(
    remoteConfigService: RemoteConfigService,
    downloadSession: IosDownloadSession,
) = ComposeUIViewController(
    configure = {
        initializeKoinForIos(remoteConfigService, downloadSession)
    },
) { App() }
