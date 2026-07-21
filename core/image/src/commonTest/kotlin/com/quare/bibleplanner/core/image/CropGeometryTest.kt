package com.quare.bibleplanner.core.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CropGeometryTest {
    @Test
    fun `fits the shorter side of the image exactly into the circle at rest`() {
        // When
        val scale = circleCoverScale(
            imageWidth = 1000,
            imageHeight = 2000,
            circleDiameter = 900f,
        )

        // Then
        assertEquals(0.9f, scale, TOLERANCE)
    }

    @Test
    fun `selects the full image width at minimum zoom on a portrait image`() {
        // When
        val crop = computeCropRect(params(zoom = 1f))

        // Then
        assertEquals(1f, crop.size, TOLERANCE)
        assertEquals(0f, crop.left, TOLERANCE)
        assertEquals(0.25f, crop.top, TOLERANCE)
    }

    @Test
    fun `shrinks the selection as the user zooms in`() {
        // When
        val crop = computeCropRect(params(zoom = 2f))

        // Then
        assertEquals(0.5f, crop.size, TOLERANCE)
        assertEquals(0.25f, crop.left, TOLERANCE)
        assertEquals(0.375f, crop.top, TOLERANCE)
    }

    @Test
    fun `never lets the selection escape the image`() {
        // When
        val crop = computeCropRect(params(zoom = 1.5f, offsetX = 100_000f, offsetY = -100_000f))

        // Then
        assertEquals(0f, crop.left, TOLERANCE)
        assertTrue(crop.top >= 0f)
        assertTrue(crop.top + (crop.size * MIN_SIDE_RATIO) <= 1f + TOLERANCE)
    }

    @Test
    fun `blocks sideways panning at minimum zoom so the image never leaves the circle`() {
        // When
        val (maxX, maxY) = maxPanOffset(
            imageWidth = 1000,
            imageHeight = 2000,
            circleDiameter = 900f,
            zoom = 1f,
        )

        // Then
        assertEquals(0f, maxX, TOLERANCE)
        assertEquals(450f, maxY, TOLERANCE)
    }

    @Test
    fun `unlocks sideways panning once the user zooms in`() {
        // When
        val (maxX, maxY) = maxPanOffset(
            imageWidth = 1000,
            imageHeight = 2000,
            circleDiameter = 900f,
            zoom = 1.5f,
        )

        // Then
        assertEquals(225f, maxX, TOLERANCE)
        assertEquals(900f, maxY, TOLERANCE)
    }

    private fun params(
        zoom: Float,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
    ) = CropParams(
        imageWidth = 1000,
        imageHeight = 2000,
        circleDiameter = 900f,
        zoom = zoom,
        offsetX = offsetX,
        offsetY = offsetY,
    )

    private companion object {
        const val TOLERANCE = 0.001f
        const val MIN_SIDE_RATIO = 0.5f
    }
}
