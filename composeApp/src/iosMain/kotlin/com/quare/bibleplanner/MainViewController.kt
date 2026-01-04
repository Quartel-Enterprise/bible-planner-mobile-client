package com.quare.bibleplanner

import androidx.compose.ui.window.ComposeUIViewController
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.di.initializeKoin
import kotlin.experimental.ExperimentalNativeApi
import org.koin.dsl.module

private var isInitialized = false

@OptIn(ExperimentalNativeApi::class)
fun MainViewController(remoteConfigService: RemoteConfigService) = ComposeUIViewController(
    configure = {
        if (!isInitialized) {
            initializeKoin(
                platformModules = listOf(
                    module {
                        single { getDatabaseBuilder() }
                        single { remoteConfigService }
                    },
                ),
            )
            configureRevenueCat(isDebug = Platform.isDebugBinary)
            isInitialized = true
        }
    },
) { App() }
