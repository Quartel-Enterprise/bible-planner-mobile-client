package com.quare.bibleplanner.feature.editprofile.fake

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace

internal class FakeImageBitmap(
    override val width: Int,
    override val height: Int,
) : ImageBitmap {
    override val colorSpace: ColorSpace get() = error("unused")
    override val hasAlpha: Boolean get() = error("unused")
    override val config: ImageBitmapConfig get() = error("unused")

    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ) = error("unused")

    override fun prepareToDraw() = error("unused")
}
