package com.quare.bibleplanner.core.image

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

private const val PNG_QUALITY = 100

actual fun ImageBitmap.encodeToPng(): ByteArray = ByteArrayOutputStream().use { stream ->
    asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
    stream.toByteArray()
}
