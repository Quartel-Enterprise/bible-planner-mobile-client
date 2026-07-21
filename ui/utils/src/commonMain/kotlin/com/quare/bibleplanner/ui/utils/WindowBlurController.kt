package com.quare.bibleplanner.ui.utils

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class WindowBlurController {
    var radius: Dp by mutableStateOf(0.dp)
}
