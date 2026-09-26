@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.quare.bibleplanner.ui.component.date

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGAffineTransformIdentity
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.UIKit.UIColor
import platform.UIKit.UIModalPresentationOverFullScreen
import platform.UIKit.UIView
import platform.UIKit.UIViewAnimationOptionCurveEaseOut
import platform.UIKit.UIViewController

private const val SCRIM_ALPHA = 0.6f
private const val ENTER_DURATION_SECONDS = 0.25
private const val EXIT_DURATION_SECONDS = 0.2
private const val ENTER_SCALE = 1.08

@Composable
internal fun NativePickerDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val presenter = LocalUIViewController.current
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val currentContent by rememberUpdatedState(content)
    val colorScheme by rememberUpdatedState(MaterialTheme.colorScheme)
    val typography by rememberUpdatedState(MaterialTheme.typography)
    val shapes by rememberUpdatedState(MaterialTheme.shapes)

    DisposableEffect(presenter) {
        val modal = ComposeUIViewController(
            configure = { opaque = false },
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                shapes = shapes,
                typography = typography,
            ) {
                PickerModalLayout(
                    onDismissRequest = { currentOnDismissRequest() },
                    content = currentContent,
                )
            }
        }
        modal.present(from = presenter)
        onDispose {
            modal.dismissAnimated()
        }
    }
}

@Composable
private fun PickerModalLayout(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .pointerInput(Unit) {
                detectTapGestures { onDismissRequest() }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures { }
            },
        ) {
            content()
        }
    }
}

private fun UIViewController.present(from: UIViewController) {
    modalPresentationStyle = UIModalPresentationOverFullScreen
    view.backgroundColor = UIColor.clearColor
    view.alpha = 0.0
    view.transform = CGAffineTransformMakeScale(ENTER_SCALE, ENTER_SCALE)
    from.findTopmostPresented().presentViewController(
        viewControllerToPresent = this,
        animated = false,
    ) {
        UIView.animateWithDuration(
            duration = ENTER_DURATION_SECONDS,
            delay = 0.0,
            options = UIViewAnimationOptionCurveEaseOut,
            animations = {
                view.alpha = 1.0
                view.transform = CGAffineTransformIdentity.readValue()
            },
            completion = null,
        )
    }
}

private fun UIViewController.dismissAnimated() {
    UIView.animateWithDuration(
        duration = EXIT_DURATION_SECONDS,
        animations = { view.alpha = 0.0 },
        completion = {
            dismissViewControllerAnimated(
                flag = false,
                completion = null,
            )
        },
    )
}

private fun UIViewController.findTopmostPresented(): UIViewController =
    presentedViewController?.findTopmostPresented() ?: this
