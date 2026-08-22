package com.quare.bibleplanner.core.image

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Encodes a bitmap captured from composition (via a graphics layer) as PNG bytes, which is the form
 * the platform share sheets take.
 */
expect fun ImageBitmap.encodeToPng(): ByteArray
