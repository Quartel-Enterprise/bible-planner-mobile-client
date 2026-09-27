package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

private const val SHEET_SCRIM_ALPHA = 0.18f

actual fun DialogProperties.toSheetDialogProperties(): DialogProperties =
    toScrimDialogProperties(scrimColor = Color.Black.copy(alpha = SHEET_SCRIM_ALPHA))

actual fun DialogProperties.toNativeAlertDialogProperties(): DialogProperties =
    toScrimDialogProperties(scrimColor = Color.Transparent)

@OptIn(ExperimentalComposeUiApi::class)
private fun DialogProperties.toScrimDialogProperties(scrimColor: Color): DialogProperties = DialogProperties(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    usePlatformInsets = usePlatformInsets,
    useSoftwareKeyboardInset = useSoftwareKeyboardInset,
    scrimColor = scrimColor,
    animateTransition = animateTransition,
)
