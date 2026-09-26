package com.quare.bibleplanner.feature.editprofile.presentation

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DecodeImageBitmapImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var imageFile: File
    private lateinit var decodeImageBitmap: DecodeImageBitmapImpl

    @BeforeTest
    fun setUp() {
        imageFile = File.createTempFile("decode", ".png").apply { deleteOnExit() }
        decodeImageBitmap = DecodeImageBitmapImpl(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        imageFile.delete()
    }

    @Test
    fun `GIVEN a png file WHEN decoding it THEN returns a bitmap with the image size`() = runTest(testDispatcher) {
        // Given
        ImageIO.write(
            BufferedImage(
                30,
                20,
                BufferedImage.TYPE_INT_RGB,
            ),
            "png",
            imageFile,
        )

        // When
        val bitmap = decodeImageBitmap(PlatformFile(imageFile)).getOrThrow()

        // Then
        assertEquals(30, bitmap.width)
        assertEquals(20, bitmap.height)
    }

    @Test
    fun `GIVEN a file that is not an image WHEN decoding it THEN fails`() = runTest(testDispatcher) {
        // Given
        imageFile.writeText("not an image")

        // When
        val result = decodeImageBitmap(PlatformFile(imageFile))

        // Then
        assertTrue(result.isFailure)
    }
}
