package com.quare.bibleplanner.ui.component.spacer

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalSpacer(size: Dp = 16.dp) {
    BaseSpacer(
        orientation = Orientation.Vertical,
        size = size,
    )
}

@Composable
fun VerticalSpacer(size: Int) {
    VerticalSpacer(size.dp)
}
