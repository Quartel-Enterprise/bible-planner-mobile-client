package com.quare.bibleplanner.android.designsystem.avatar

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

/*
 * Why: every avatar of the same photo bytes (the tab icon, the expanded photo, the bar and rail swapping on rotation)
 * shares one decode, keyed by the array's reference, so a newly composed one shows the photo on its first frame.
 */
internal object DecodedAvatarPhotoCache {
    private const val MAX_SIZE_BYTES = 16 * 1024 * 1024

    private val cache = object : LruCache<ByteArray, DecodedAvatarPhoto>(MAX_SIZE_BYTES) {
        override fun sizeOf(
            key: ByteArray,
            value: DecodedAvatarPhoto,
        ): Int = key.size + value.bitmap.asAndroidBitmap().byteCount
    }

    fun findOrNull(bytes: ByteArray): DecodedAvatarPhoto? = cache.get(bytes)

    fun findCoveringOrNull(
        bytes: ByteArray,
        targetSizePx: Int,
    ): ImageBitmap? = findOrNull(bytes)?.takeIf { it.isCovering(targetSizePx) }?.bitmap

    // Why: a full-resolution camera photo would hold megabytes of pixels for an avatar a few dozen pixels wide.
    fun decode(
        bytes: ByteArray,
        targetSizePx: Int,
    ): ImageBitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            bounds,
        )
        val sampleSize = getSampleSize(
            shorterSidePx = minOf(bounds.outWidth, bounds.outHeight),
            targetSizePx = targetSizePx,
        )
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory
            .decodeByteArray(
                bytes,
                0,
                bytes.size,
                options,
            )?.asImageBitmap() ?: return null
        val decoded = DecodedAvatarPhoto(
            bitmap = bitmap,
            sampleSize = sampleSize,
        )
        // Why: keeps the sharpest decode, so a small tab icon doesn't replace the expanded photo's.
        synchronized(this) {
            val cached = cache.get(bytes)
            if (cached == null || cached.sampleSize > sampleSize) cache.put(bytes, decoded)
        }
        return bitmap
    }

    // Why: the photo is cropped to a circle, so its shorter side must still cover the target after sampling.
    private fun getSampleSize(
        shorterSidePx: Int,
        targetSizePx: Int,
    ): Int {
        var sampleSize = 1
        while (targetSizePx > 0 && shorterSidePx / (sampleSize * 2) >= targetSizePx) {
            sampleSize *= 2
        }
        return sampleSize
    }
}

internal class DecodedAvatarPhoto(
    val bitmap: ImageBitmap,
    val sampleSize: Int,
) {
    fun isCovering(targetSizePx: Int): Boolean = sampleSize == 1 || minOf(bitmap.width, bitmap.height) >= targetSizePx
}
