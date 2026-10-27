package com.quare.bibleplanner.core.image

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/*
 * Why: a full-resolution camera photo (48 MP and up) is ~190 MB as a bitmap, over the ~100 MB
 * Android refuses to draw, so every decoded picture is capped before it reaches the UI.
 */
internal class DecodeImageBitmapImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : DecodeImageBitmap {
    override suspend fun invoke(file: PlatformFile): Result<ImageBitmap> = suspendRunCatching {
        withContext(dispatcher) { file.readBytes().decodeSampledImageBitmap(MAX_DIMENSION_PX).toCapped() }
    }

    private fun ImageBitmap.toCapped(): ImageBitmap {
        val longestSide = maxOf(width, height)
        if (longestSide <= MAX_DIMENSION_PX) return this
        val scale = MAX_DIMENSION_PX.toFloat() / longestSide
        val targetSize = IntSize(
            width = (width * scale).roundToInt().coerceAtLeast(1),
            height = (height * scale).roundToInt().coerceAtLeast(1),
        )
        val capped = ImageBitmap(
            width = targetSize.width,
            height = targetSize.height,
        )
        Canvas(capped).drawImageRect(
            image = this,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(width, height),
            dstOffset = IntOffset.Zero,
            dstSize = targetSize,
            paint = Paint().apply { filterQuality = FilterQuality.High },
        )
        return capped
    }

    private companion object {
        const val MAX_DIMENSION_PX = 2048
    }
}
