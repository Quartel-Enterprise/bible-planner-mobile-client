package com.quare.bibleplanner

import androidx.compose.ui.window.ComposeUIViewController
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import org.koin.dsl.module


private var isInitialized = false

fun MainViewController() = ComposeUIViewController(
    configure = {
        if (!isInitialized) {
            configureRevenueCat()
            initializeKoin(
                platformModules = listOf(
                    module {
                        single { getDatabaseBuilder() }
                    },
                ),
            )
            isInitialized = true
        }
    },
) { App() }
