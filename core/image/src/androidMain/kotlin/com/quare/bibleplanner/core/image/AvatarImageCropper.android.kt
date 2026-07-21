package com.quare.bibleplanner.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream

actual fun avatarImageCropper(): AvatarImageCropper = AndroidAvatarImageCropper()

internal class AndroidAvatarImageCropper : AvatarImageCropper {
    override fun invoke(
        source: ByteArray,
        params: CropParams,
    ): ByteArray {
        val crop = computeCropRect(params)
        val decoded = BitmapFactory
            .decodeByteArray(source, 0, source.size, decodeOptions(source))
            ?: throw UnsupportedImageFormatException()
        val output = createBitmap(
            width = AVATAR_OUTPUT_SIZE_PX,
            height = AVATAR_OUTPUT_SIZE_PX,
        )
        Canvas(output).drawBitmap(
            decoded,
            decoded.cropRect(crop),
            Rect(0, 0, AVATAR_OUTPUT_SIZE_PX, AVATAR_OUTPUT_SIZE_PX),
            null,
        )
        decoded.recycle()
        return ByteArrayOutputStream().use { stream ->
            output.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, stream)
            output.recycle()
            stream.toByteArray()
        }
    }

    private fun Bitmap.cropRect(crop: NormalizedCropRect): Rect {
        val left = (crop.left * width).toInt()
        val top = (crop.top * height).toInt()
        val side = (crop.size * minOf(width, height)).toInt().coerceAtLeast(1)
        return Rect(
            left.coerceIn(0, width - 1),
            top.coerceIn(0, height - 1),
            (left + side).coerceAtMost(width),
            (top + side).coerceAtMost(height),
        )
    }

    private fun decodeOptions(source: ByteArray): BitmapFactory.Options {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(source, 0, source.size, bounds)
        return BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(
                width = bounds.outWidth,
                height = bounds.outHeight,
                targetPx = AVATAR_OUTPUT_SIZE_PX,
            )
        }
    }
}
