package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.ui.window.DialogProperties

expect fun DialogProperties.toSheetDialogProperties(): DialogProperties

expect fun DialogProperties.toNativeAlertDialogProperties(): DialogProperties
