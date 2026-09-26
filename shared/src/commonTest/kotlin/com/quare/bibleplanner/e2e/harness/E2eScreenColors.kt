package com.quare.bibleplanner.e2e.harness

import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot

private const val TIMEOUT_MILLIS = 10_000L
private const val SAMPLE_STEP = 8
private const val LIGHT_SCREEN_MIN_LUMINANCE = 0.6f
private const val DARK_SCREEN_MAX_LUMINANCE = 0.25f

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.awaitLightScreen() {
    waitUntil(timeoutMillis = TIMEOUT_MILLIS) { averageLuminance() > LIGHT_SCREEN_MIN_LUMINANCE }
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.awaitDarkScreen() {
    waitUntil(timeoutMillis = TIMEOUT_MILLIS) { averageLuminance() < DARK_SCREEN_MAX_LUMINANCE }
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.averageLuminance(): Float {
    val pixels = onRoot().captureToImage().toPixelMap()
    val samples = (0 until pixels.width step SAMPLE_STEP).flatMap { x ->
        (0 until pixels.height step SAMPLE_STEP).map { y -> pixels[x, y].luminance() }
    }
    return samples.average().toFloat()
}
