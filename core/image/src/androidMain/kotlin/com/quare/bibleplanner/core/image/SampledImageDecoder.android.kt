package com.quare.bibleplanner.core.image

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun ByteArray.decodeSampledImageBitmap(maxDimensionPx: Int): ImageBitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(this, 0, size, bounds)
    var sampleSize = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxDimensionPx) {
        sampleSize *= 2
    }
    return decodeUprightBitmap(sampleSize)?.asImageBitmap() ?: throw UnsupportedImageFormatException()
}
