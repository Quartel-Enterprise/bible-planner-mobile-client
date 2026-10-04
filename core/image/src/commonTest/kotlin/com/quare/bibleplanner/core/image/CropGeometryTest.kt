package com.quare.bibleplanner.core.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CropGeometryTest {
    @Test
    fun `GIVEN a portrait image WHEN getting the cover scale THEN fits its shorter side exactly into the circle`() {
        // Given
        val imageWidth = 1000
        val imageHeight = 2000
        val circleDiameter = 900f

        // When
        val scale = getCircleCoverScale(
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            circleDiameter = circleDiameter,
        )

        // Then
        assertEquals(0.9f, scale, TOLERANCE)
    }

    @Test
    fun `GIVEN a portrait image at minimum zoom WHEN computing the crop rect THEN selects the full image width`() {
        // Given
        val cropParams = params(zoom = 1f)

        // When
        val crop = computeCropRect(cropParams)

        // Then
        assertEquals(1f, crop.size, TOLERANCE)
        assertEquals(0f, crop.left, TOLERANCE)
        assertEquals(0.25f, crop.top, TOLERANCE)
    }

    @Test
    fun `GIVEN a zoomed in image WHEN computing the crop rect THEN shrinks the selection`() {
        // Given
        val cropParams = params(zoom = 2f)

        // When
        val crop = computeCropRect(cropParams)

        // Then
        assertEquals(0.5f, crop.size, TOLERANCE)
        assertEquals(0.25f, crop.left, TOLERANCE)
        assertEquals(0.375f, crop.top, TOLERANCE)
    }

    @Test
    fun `GIVEN a huge pan offset WHEN computing the crop rect THEN keeps the selection inside the image`() {
        // Given
        val cropParams = params(zoom = 1.5f, offsetX = 100_000f, offsetY = -100_000f)

        // When
        val crop = computeCropRect(cropParams)

        // Then
        assertEquals(0f, crop.left, TOLERANCE)
        assertTrue(crop.top >= 0f)
        assertTrue(crop.top + (crop.size * MIN_SIDE_RATIO) <= 1f + TOLERANCE)
    }

    @Test
    fun `GIVEN a portrait photo turned sideways WHEN computing the crop rect THEN selects the full image height`() {
        // Given
        val cropParams = params(zoom = 1f, orientation = quarterTurned())

        // When
        val crop = computeCropRect(cropParams)

        // Then
        assertEquals(1f, crop.size, TOLERANCE)
        assertEquals(0.25f, crop.left, TOLERANCE)
        assertEquals(0f, crop.top, TOLERANCE)
    }

    @Test
    fun `GIVEN minimum zoom WHEN getting the max pan offset THEN blocks sideways panning`() {
        // Given
        val zoom = 1f
        val orientation = original()

        // When
        val (maxX, maxY) = getMaxPanOffset(
            imageWidth = 1000,
            imageHeight = 2000,
            circleDiameter = 900f,
            zoom = zoom,
            orientation = orientation,
        )

        // Then
        assertEquals(0f, maxX, TOLERANCE)
        assertEquals(450f, maxY, TOLERANCE)
    }

    @Test
    fun `GIVEN a zoomed in image WHEN getting the max pan offset THEN unlocks sideways panning`() {
        // Given
        val zoom = 1.5f
        val orientation = original()

        // When
        val (maxX, maxY) = getMaxPanOffset(
            imageWidth = 1000,
            imageHeight = 2000,
            circleDiameter = 900f,
            zoom = zoom,
            orientation = orientation,
        )

        // Then
        assertEquals(225f, maxX, TOLERANCE)
        assertEquals(900f, maxY, TOLERANCE)
    }

    @Test
    fun `GIVEN a photo turned sideways WHEN getting the max pan offset THEN swaps the panning axes`() {
        // Given
        val zoom = 1f
        val orientation = quarterTurned()

        // When
        val (maxX, maxY) = getMaxPanOffset(
            imageWidth = 1000,
            imageHeight = 2000,
            circleDiameter = 900f,
            zoom = zoom,
            orientation = orientation,
        )

        // Then
        assertEquals(450f, maxX, TOLERANCE)
        assertEquals(0f, maxY, TOLERANCE)
    }

    private fun params(
        zoom: Float,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        orientation: PhotoOrientation = original(),
    ): CropParams = CropParams(
        imageWidth = 1000,
        imageHeight = 2000,
        circleDiameter = 900f,
        zoom = zoom,
        offsetX = offsetX,
        offsetY = offsetY,
        orientation = orientation,
    )

    private fun original(): PhotoOrientation = PhotoOrientation(
        rotationDegrees = PhotoOrientation.NO_ROTATION,
        isFlippedHorizontally = false,
        isFlippedVertically = false,
    )

    private fun quarterTurned(): PhotoOrientation = original().rotateQuarterTurn()

    private companion object {
        const val TOLERANCE = 0.001f
        const val MIN_SIDE_RATIO = 0.5f
    }
}
