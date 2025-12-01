package com.quare.bibleplanner

import androidx.compose.ui.window.ComposeUIViewController
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import org.koin.dsl.module

fun MainViewController() = ComposeUIViewController(
    configure = {
        initializeKoin(
            platformModules = listOf(
                module {
                    single { getDatabaseBuilder() }
                },
            ),
        )
    },
) { App() }
