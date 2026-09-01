package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun WindowContentBlurEffect(radius: Dp) {
    val controller = LocalWindowBlurController.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(controller, radius, lifecycle) {
        controller.radius = radius
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> controller.radius = radius

                Lifecycle.Event.ON_STOP,
                Lifecycle.Event.ON_DESTROY,
                -> controller.radius = 0.dp

                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            controller.radius = 0.dp
        }
    }
}
