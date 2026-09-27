package com.quare.bibleplanner.ui.testing.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.quare.bibleplanner.ui.testing.setUiTestContent
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.getenv

private const val OUTPUT_DIRECTORY_VARIABLE = "APPLE_SCREENSHOTS_OUTPUT"
private const val APPLE_LANGUAGES_KEY = "AppleLanguages"

@OptIn(ExperimentalTestApi::class)
fun captureAppleScreenshots(
    fileName: String,
    beforeCapture: suspend ComposeUiTest.(locale: String) -> Unit = {},
    content: @Composable (slot: AppleScreenshotSlot, locale: String) -> Unit,
) {
    AppleScreenshotSlot.entries.forEach { slot ->
        storeScreenshotLocales.forEach { locale ->
            captureAppleScreenshot(
                slot = slot,
                locale = locale,
                fileName = fileName,
                beforeCapture = beforeCapture,
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalTestApi::class)
private fun captureAppleScreenshot(
    slot: AppleScreenshotSlot,
    locale: String,
    fileName: String,
    beforeCapture: suspend ComposeUiTest.(locale: String) -> Unit,
    content: @Composable (slot: AppleScreenshotSlot, locale: String) -> Unit,
) {
    NSUserDefaults.standardUserDefaults.setObject(
        listOf(locale),
        forKey = APPLE_LANGUAGES_KEY,
    )
    runSkikoComposeUiTest(
        size = Size(
            width = slot.width.value * slot.density,
            height = slot.height.value * slot.density,
        ),
        density = Density(slot.density),
    ) {
        setUiTestContent { content(slot, locale) }
        waitForIdle()
        beforeCapture(locale)
        waitForIdle()
        val png = Image
            .makeFromBitmap(onRoot().captureToImage().asSkiaBitmap())
            .encodeToData(EncodedImageFormat.PNG)
            ?.bytes
            ?: error("Could not encode the $fileName capture for ${slot.name} in $locale as PNG")
        writePng(
            directory = "${getOutputDirectory()}/${slot.directoryName}/$locale",
            fileName = "$fileName.png",
            png = png,
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun getOutputDirectory(): String = getenv(OUTPUT_DIRECTORY_VARIABLE)?.toKString()
    ?: error(
        "$OUTPUT_DIRECTORY_VARIABLE is not set: run the captures through the iosSimulatorArm64AppleScreenshotsTest task",
    )

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun writePng(
    directory: String,
    fileName: String,
    png: ByteArray,
) {
    NSFileManager.defaultManager.createDirectoryAtPath(
        path = directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    val written = png.usePinned { pinned ->
        NSData
            .create(
                bytes = pinned.addressOf(0),
                length = png.size.toULong(),
            ).writeToFile(
                path = "$directory/$fileName",
                atomically = true,
            )
    }
    check(written) { "Could not write $directory/$fileName" }
}
