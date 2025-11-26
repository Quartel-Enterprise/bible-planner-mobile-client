package com.quare.bibleplanner

import androidx.compose.ui.window.ComposeUIViewController
import com.quare.bibleplanner.core.provider.koin.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initializeKoin()
    },
) { App() }
