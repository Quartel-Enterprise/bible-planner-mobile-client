package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.ui.window.DialogProperties

actual fun createCardDialogProperties(): DialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    decorFitsSystemWindows = false,
)

actual fun DialogProperties.toNativeAlertDialogProperties(): DialogProperties = this
