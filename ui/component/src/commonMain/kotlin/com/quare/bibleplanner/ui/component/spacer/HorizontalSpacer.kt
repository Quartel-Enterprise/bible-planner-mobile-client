package com.quare.bibleplanner.ui.component.spacer

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalSpacer(size: Dp = 16.dp) {
    BaseSpacer(
        orientation = Orientation.Horizontal,
        size = size,
    )
}

@Composable
fun HorizontalSpacer(size: Int) {
    HorizontalSpacer(size.dp)
}
