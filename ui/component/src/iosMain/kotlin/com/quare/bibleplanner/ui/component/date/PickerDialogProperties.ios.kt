package com.quare.bibleplanner.ui.component.date

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
actual val pickerDialogProperties: DialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    scrimColor = Color.Transparent,
    animateTransition = false,
)
