package com.quare.bibleplanner.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import platform.UIKit.UIViewController

@Composable
internal actual fun PlatformUiTestContent(content: @Composable () -> Unit) {
    val viewController = remember { UIViewController(nibName = null, bundle = null) }
    CompositionLocalProvider(
        LocalUIViewController provides viewController,
        content = content,
    )
}
