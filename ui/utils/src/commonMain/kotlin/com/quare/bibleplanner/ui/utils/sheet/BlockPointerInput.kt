package com.quare.bibleplanner.ui.utils.sheet

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.blockPointerInput(isBlocked: Boolean): Modifier = if (isBlocked) {
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent(PointerEventPass.Initial).changes.forEach(PointerInputChange::consume)
            }
        }
    }
} else {
    this
}
