package com.quare.bibleplanner

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.quare.bibleplanner.di.initializeKoinForWeb

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeKoinForWeb()
    ComposeViewport(
        configure = { isA11YEnabled = true },
    ) {
        AppRoot()
    }
}
