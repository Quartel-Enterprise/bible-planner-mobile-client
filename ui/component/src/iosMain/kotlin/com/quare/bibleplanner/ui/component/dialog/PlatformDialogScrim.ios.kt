package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
actual fun createCardDialogProperties(): DialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    usePlatformInsets = false,
    useSoftwareKeyboardInset = false,
    scrimColor = Color.Transparent,
    animateTransition = false,
)

@OptIn(ExperimentalComposeUiApi::class)
actual fun DialogProperties.toNativeAlertDialogProperties(): DialogProperties = DialogProperties(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    usePlatformInsets = usePlatformInsets,
    useSoftwareKeyboardInset = useSoftwareKeyboardInset,
    scrimColor = Color.Transparent,
    animateTransition = animateTransition,
)
