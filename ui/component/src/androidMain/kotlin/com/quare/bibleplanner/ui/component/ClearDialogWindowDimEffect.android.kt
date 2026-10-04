package com.quare.bibleplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

@Composable
actual fun ClearDialogWindowDimEffect() {
    val view = LocalView.current
    SideEffect {
        val dialogWindowProvider = view.parent as? DialogWindowProvider
        dialogWindowProvider?.window?.setDimAmount(0f)
    }
}
