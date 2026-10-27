package com.quare.bibleplanner.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

private const val QUARTER_TURN = 90f
private const val HALF_TURN = 180f
private const val THREE_QUARTER_TURN = 270f

/*
 * Why: BitmapFactory ignores the EXIF orientation many camera apps write instead of rotating the
 * pixels, so a portrait photo would show sideways; the preview and the avatar crop both go through
 * here so they stay aligned.
 */
internal fun ByteArray.decodeUprightBitmap(sampleSize: Int): Bitmap? {
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val decoded = BitmapFactory.decodeByteArray(this, 0, size, options) ?: return null
    val matrix = getExifMatrix() ?: return decoded
    val upright = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    if (upright !== decoded) decoded.recycle()
    return upright
}

private fun ByteArray.getExifMatrix(): Matrix? {
    val orientation = runCatching {
        ExifInterface(inputStream()).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> Matrix().apply { postRotate(QUARTER_TURN) }

        ExifInterface.ORIENTATION_ROTATE_180 -> Matrix().apply { postRotate(HALF_TURN) }

        ExifInterface.ORIENTATION_ROTATE_270 -> Matrix().apply { postRotate(THREE_QUARTER_TURN) }

        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> Matrix().apply { postScale(-1f, 1f) }

        ExifInterface.ORIENTATION_FLIP_VERTICAL -> Matrix().apply { postScale(1f, -1f) }

        ExifInterface.ORIENTATION_TRANSPOSE -> Matrix().apply {
            postRotate(QUARTER_TURN)
            postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_TRANSVERSE -> Matrix().apply {
            postRotate(THREE_QUARTER_TURN)
            postScale(-1f, 1f)
        }

        else -> null
    }
}
