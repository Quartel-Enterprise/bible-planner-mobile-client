package com.quare.bibleplanner.core.image

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

private const val QUALITY_SCALE = 100.0

actual fun avatarImageCropper(): AvatarImageCropper = IosAvatarImageCropper()

@OptIn(ExperimentalForeignApi::class)
internal class IosAvatarImageCropper : AvatarImageCropper {
    override fun invoke(
        source: ByteArray,
        params: CropParams,
    ): ByteArray {
        val crop = computeCropRect(params)
        val image = UIImage.imageWithData(source.toNsData()) ?: throw UnsupportedImageFormatException()
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
