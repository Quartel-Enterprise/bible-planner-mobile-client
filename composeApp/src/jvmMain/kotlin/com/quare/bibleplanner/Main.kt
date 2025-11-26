package com.quare.bibleplanner

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.quare.bibleplanner.core.provider.koin.initializeKoin

fun main() = application {
    initializeKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "BiblePlanner",
    ) {
        App()
    }
}
