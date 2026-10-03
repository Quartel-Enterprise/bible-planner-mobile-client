package com.quare.bibleplanner.core.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.encodeToPng(): ByteArray = Image
    .makeFromBitmap(asSkiaBitmap())
    .encodeToData(EncodedImageFormat.PNG)
    ?.bytes
    ?: ByteArray(0)
