package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.ui.window.DialogProperties

actual fun DialogProperties.toSheetDialogProperties(): DialogProperties = this

actual fun DialogProperties.toNativeAlertDialogProperties(): DialogProperties = this
