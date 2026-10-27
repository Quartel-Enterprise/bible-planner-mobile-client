package com.quare.bibleplanner.core.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap

internal actual fun ByteArray.decodeSampledImageBitmap(maxDimensionPx: Int): ImageBitmap = decodeToImageBitmap()
