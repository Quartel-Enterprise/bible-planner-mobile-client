package com.quare.bibleplanner.ui.testing.screenshots

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppleScreenshotSlot(
    val directoryName: String,
    val width: Dp,
    val height: Dp,
    val density: Float,
    val statusBarHeight: Dp,
) {
    IPHONE_6_5(
        directoryName = "iphone65",
        width = 428.dp,
        height = 926.dp,
        density = 3f,
        statusBarHeight = 62.dp,
    ),
    IPHONE_6_7(
        directoryName = "iphone67",
        width = 430.dp,
        height = 932.dp,
        density = 3f,
        statusBarHeight = 62.dp,
    ),
    IPAD_13(
        directoryName = "ipad13",
        width = 1024.dp,
        height = 1366.dp,
        density = 2f,
        statusBarHeight = 24.dp,
    ),
}
