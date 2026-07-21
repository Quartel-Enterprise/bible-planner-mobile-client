package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WindowContentBlurEffect(radius: Dp) {
    val controller = LocalWindowBlurController.current
    DisposableEffect(controller, radius) {
        controller.radius = radius
        onDispose { controller.radius = 0.dp }
    }
}
