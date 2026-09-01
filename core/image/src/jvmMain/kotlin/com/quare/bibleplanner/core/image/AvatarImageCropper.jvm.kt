package com.quare.bibleplanner.core.image

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.PI

private const val JPEG_FORMAT = "jpg"
private const val HALF = 2
private const val STRAIGHT_ANGLE_DEGREES = 180.0

actual fun createAvatarImageCropper(): AvatarImageCropper = JvmAvatarImageCropper()

internal class JvmAvatarImageCropper : AvatarImageCropper {
    override fun invoke(
        source: ByteArray,
        params: CropParams,
    ): ByteArray {
        val crop = computeCropRect(params)
        val decoded = ByteArrayInputStream(source).use(ImageIO::read)
            ?: throw UnsupportedImageFormatException()
        val oriented = decoded.orient(params.orientation)
        val side = (crop.size * minOf(oriented.width, oriented.height)).toInt().coerceAtLeast(1)
        val left = (crop.left * oriented.width).toInt().coerceIn(0, oriented.width - 1)
        val top = (crop.top * oriented.height).toInt().coerceIn(0, oriented.height - 1)
        val cropped = oriented.getSubimage(
            left,
            top,
            side.coerceAtMost(oriented.width - left),
            side.coerceAtMost(oriented.height - top),
        )

        val output = BufferedImage(AVATAR_OUTPUT_SIZE_PX, AVATAR_OUTPUT_SIZE_PX, BufferedImage.TYPE_INT_RGB)
        output.createGraphics().run {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            drawImage(cropped, 0, 0, AVATAR_OUTPUT_SIZE_PX, AVATAR_OUTPUT_SIZE_PX, null)
            dispose()
        }

        return ByteArrayOutputStream().use { stream ->
            ImageIO.write(output, JPEG_FORMAT, stream)
            stream.toByteArray()
        }
    }

    private fun BufferedImage.orient(orientation: PhotoOrientation): BufferedImage {
        if (orientation.isOriginal) return this
        val sourceWidth = width
        val sourceHeight = height
        val targetWidth = if (orientation.isQuarterTurned) sourceHeight else sourceWidth
        val targetHeight = if (orientation.isQuarterTurned) sourceWidth else sourceHeight
        val target = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        target.createGraphics().run {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            translate(targetWidth / HALF, targetHeight / HALF)
            rotate(orientation.rotationDegrees * PI / STRAIGHT_ANGLE_DEGREES)
            scale(orientation.horizontalScale.toDouble(), orientation.verticalScale.toDouble())
            drawImage(this@orient, -sourceWidth / HALF, -sourceHeight / HALF, null)
            dispose()
        }
        return target
    }
}
