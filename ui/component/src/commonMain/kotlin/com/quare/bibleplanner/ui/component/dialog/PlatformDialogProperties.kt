package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.ui.window.DialogProperties

expect fun createCardDialogProperties(): DialogProperties

expect fun DialogProperties.toNativeAlertDialogProperties(): DialogProperties
