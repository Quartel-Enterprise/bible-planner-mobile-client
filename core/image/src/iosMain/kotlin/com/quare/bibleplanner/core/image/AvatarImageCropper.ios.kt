package com.quare.bibleplanner.core.image

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGContextRotateCTM
import platform.CoreGraphics.CGContextScaleCTM
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import kotlin.math.PI

private const val QUALITY_SCALE = 100.0
private const val HALF = 2.0
private const val STRAIGHT_ANGLE_DEGREES = 180.0

actual fun avatarImageCropper(): AvatarImageCropper = IosAvatarImageCropper()

@OptIn(ExperimentalForeignApi::class)
internal class IosAvatarImageCropper : AvatarImageCropper {
    override fun invoke(
        source: ByteArray,
        params: CropParams,
    ): ByteArray {
        val crop = computeCropRect(params)
        val decoded = UIImage.imageWithData(source.toNsData()) ?: throw UnsupportedImageFormatException()
        val image = decoded.oriented(params.orientation)
        val width = image.size.useContents { width }
        val height = image.size.useContents { height }
        val side = crop.size * minOf(width, height)
        val scale = AVATAR_OUTPUT_SIZE_PX / side
        val target = CGSizeMake(AVATAR_OUTPUT_SIZE_PX.toDouble(), AVATAR_OUTPUT_SIZE_PX.toDouble())

        UIGraphicsBeginImageContextWithOptions(target, true, 1.0)
        image.drawInRect(
            CGRectMake(
                x = -crop.left * width * scale,
                y = -crop.top * height * scale,
                width = width * scale,
                height = height * scale,
            ),
        )
        val cropped = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        val jpeg = cropped?.let { UIImageJPEGRepresentation(it, AVATAR_JPEG_QUALITY / QUALITY_SCALE) }
            ?: throw UnsupportedImageFormatException()
        return jpeg.toByteArray()
    }

    private fun UIImage.oriented(orientation: PhotoOrientation): UIImage {
        if (orientation.isOriginal) return this
        val width = size.useContents { width }
        val height = size.useContents { height }
        val targetWidth = if (orientation.isQuarterTurned) height else width
        val targetHeight = if (orientation.isQuarterTurned) width else height

        UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetWidth, targetHeight), true, 1.0)
        val context = UIGraphicsGetCurrentContext()
        CGContextTranslateCTM(context, targetWidth / HALF, targetHeight / HALF)
        CGContextRotateCTM(context, orientation.rotationDegrees * PI / STRAIGHT_ANGLE_DEGREES)
        CGContextScaleCTM(
            context,
            orientation.horizontalScale.toDouble(),
            orientation.verticalScale.toDouble(),
        )
        drawInRect(
            CGRectMake(
                x = -width / HALF,
                y = -height / HALF,
                width = width,
                height = height,
            ),
        )
        val rotated = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return rotated ?: this
    }

    private fun ByteArray.toNsData(): NSData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

    private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
        if (isNotEmpty()) {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), bytes, length)
            }
        }
    }
}
