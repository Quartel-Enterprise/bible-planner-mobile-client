package com.quare.bibleplanner

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.notification.IosBibleVersionDownloadNotifier
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi

private var isInitialized = false

@OptIn(ExperimentalNativeApi::class)
fun MainViewController(remoteConfigService: RemoteConfigService) = ComposeUIViewController(
    configure = {
        if (!isInitialized) {
            try {
                initializeKoin(
                    platformModules = listOf(
                        module {
                            single { getDatabaseBuilder() }
                            single { remoteConfigService }
                            single { IosBibleVersionDownloadNotifier() }.bind<BibleVersionDownloadNotifier>()
                        },
                    ),
                )
                configureRevenueCat(isDebug = Platform.isDebugBinary)
                isInitialized = true
            } catch (e: Exception) {
                Logger.e(e) { "Error initializing App" }
            }
        }
    },
) { App() }
