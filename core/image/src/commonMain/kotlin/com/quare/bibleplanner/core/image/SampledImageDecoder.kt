package com.quare.bibleplanner.core.image

import androidx.compose.ui.graphics.ImageBitmap

// Why: Android can sample the photo down while decoding, so it never holds the full-resolution bitmap.
internal expect fun ByteArray.decodeSampledImageBitmap(maxDimensionPx: Int): ImageBitmap
