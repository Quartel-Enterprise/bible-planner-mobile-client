package com.quare.bibleplanner.core.image

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private const val JPEG_FORMAT = "jpg"

actual fun avatarImageCropper(): AvatarImageCropper = JvmAvatarImageCropper()

internal class JvmAvatarImageCropper : AvatarImageCropper {
    override fun invoke(
        source: ByteArray,
        params: CropParams,
    ): ByteArray {
        val crop = computeCropRect(params)
        val decoded = ByteArrayInputStream(source).use(ImageIO::read)
            ?: throw UnsupportedImageFormatException()
        val side = (crop.size * minOf(decoded.width, decoded.height)).toInt().coerceAtLeast(1)
        val left = (crop.left * decoded.width).toInt().coerceIn(0, decoded.width - 1)
        val top = (crop.top * decoded.height).toInt().coerceIn(0, decoded.height - 1)
        val cropped = decoded.getSubimage(
            left,
            top,
            side.coerceAtMost(decoded.width - left),
            side.coerceAtMost(decoded.height - top),
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
}
