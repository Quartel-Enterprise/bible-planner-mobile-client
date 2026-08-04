package com.quare.bibleplanner.core.image

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class JvmAvatarImageCropperTest {
    private val cropper = JvmAvatarImageCropper()

    @Test
    fun `keeps every quadrant in place when the photo is untouched`() {
        // When
        val output = crop(original())

        // Then
        assertQuadrants(
            output = output,
            topLeft = Color.RED,
            topRight = Color.GREEN,
            bottomLeft = Color.BLUE,
            bottomRight = Color.WHITE,
        )
    }

    @Test
    fun `swaps the left and right quadrants when the photo is mirrored horizontally`() {
        // When
        val output = crop(original().flippedHorizontally())

        // Then
        assertQuadrants(
            output = output,
            topLeft = Color.GREEN,
            topRight = Color.RED,
            bottomLeft = Color.WHITE,
            bottomRight = Color.BLUE,
        )
    }

    @Test
    fun `swaps the top and bottom quadrants when the photo is mirrored vertically`() {
        // When
        val output = crop(original().flippedVertically())

        // Then
        assertQuadrants(
            output = output,
            topLeft = Color.BLUE,
            topRight = Color.WHITE,
            bottomLeft = Color.RED,
            bottomRight = Color.GREEN,
        )
    }

    @Test
    fun `turns the quadrants clockwise on a quarter turn`() {
        // When
        val output = crop(original().rotatedQuarterTurn())

        // Then
        assertQuadrants(
            output = output,
            topLeft = Color.BLUE,
            topRight = Color.RED,
            bottomLeft = Color.WHITE,
            bottomRight = Color.GREEN,
        )
    }

    private fun crop(orientation: PhotoOrientation): BufferedImage {
        val cropped = cropper(
            source = quadrantImage(),
            params = CropParams(
                imageWidth = SOURCE_SIZE,
                imageHeight = SOURCE_SIZE,
                circleDiameter = SOURCE_SIZE.toFloat(),
                zoom = 1f,
                offsetX = 0f,
                offsetY = 0f,
                orientation = orientation,
            ),
        )
        return ByteArrayInputStream(cropped).use(ImageIO::read)
    }

    private fun quadrantImage(): ByteArray {
        val image = BufferedImage(SOURCE_SIZE, SOURCE_SIZE, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().run {
            paint(Color.RED, left = 0, top = 0)
            paint(Color.GREEN, left = SOURCE_SIZE / 2, top = 0)
            paint(Color.BLUE, left = 0, top = SOURCE_SIZE / 2)
            paint(Color.WHITE, left = SOURCE_SIZE / 2, top = SOURCE_SIZE / 2)
            dispose()
        }
        return ByteArrayOutputStream().use { stream ->
            ImageIO.write(image, "jpg", stream)
            stream.toByteArray()
        }
    }

    private fun Graphics2D.paint(
        fill: Color,
        left: Int,
        top: Int,
    ) {
        color = fill
        fillRect(left, top, SOURCE_SIZE / 2, SOURCE_SIZE / 2)
    }

    private fun assertQuadrants(
        output: BufferedImage,
        topLeft: Color,
        topRight: Color,
        bottomLeft: Color,
        bottomRight: Color,
    ) {
        val near = output.width / 4
        val far = output.width * 3 / 4
        assertColor(expected = topLeft, actual = Color(output.getRGB(near, near)))
        assertColor(expected = topRight, actual = Color(output.getRGB(far, near)))
        assertColor(expected = bottomLeft, actual = Color(output.getRGB(near, far)))
        assertColor(expected = bottomRight, actual = Color(output.getRGB(far, far)))
    }

    private fun assertColor(
        expected: Color,
        actual: Color,
    ) {
        val distance = abs(expected.red - actual.red) +
            abs(expected.green - actual.green) +
            abs(expected.blue - actual.blue)
        assertTrue(distance <= JPEG_TOLERANCE, "expected $expected but was $actual")
    }

    private fun original(): PhotoOrientation = PhotoOrientation(
        rotationDegrees = PhotoOrientation.NO_ROTATION,
        isFlippedHorizontally = false,
        isFlippedVertically = false,
    )

    private companion object {
        const val SOURCE_SIZE = 400
        const val JPEG_TOLERANCE = 60
    }
}
