package com.quare.bibleplanner.ui.testing

import androidx.compose.runtime.Composable

@Composable
internal actual fun ProvidePlatformUiTestLocals(content: @Composable () -> Unit) {
    content()
}
