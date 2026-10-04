package com.quare.bibleplanner.core.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhotoOrientationTest {
    @Test
    fun `GIVEN the original photo WHEN turning it four quarter turns THEN returns to the original photo`() {
        // Given
        val original = original()

        // When
        val turned = original
            .rotateQuarterTurn()
            .rotateQuarterTurn()
            .rotateQuarterTurn()
            .rotateQuarterTurn()

        // Then
        assertEquals(original, turned)
        assertTrue(turned.isOriginal)
    }

    @Test
    fun `GIVEN the original photo WHEN turning it twice THEN reports sideways only after the first quarter turn`() {
        // Given
        val original = original()

        // When
        val quarter = original.rotateQuarterTurn()
        val half = quarter.rotateQuarterTurn()

        // Then
        assertTrue(quarter.isQuarterTurned)
        assertFalse(half.isQuarterTurned)
    }

    @Test
    fun `GIVEN a mirrored photo WHEN applying the same flip again THEN undoes the mirroring`() {
        // Given
        val flipped = original().flipHorizontally()

        // When
        val unflipped = flipped.flipHorizontally()

        // Then
        assertTrue(unflipped.isOriginal)
    }

    @Test
    fun `GIVEN a photo turned sideways WHEN flipping it horizontally THEN mirrors it along the screen axis`() {
        // Given
        // Turned sideways, the photo axes are swapped on screen
        val sideways = original().rotateQuarterTurn()

        // When
        val flipped = sideways.flipHorizontally()

        // Then
        // Flipping the photo vertically is what mirrors it horizontally on screen
        assertTrue(flipped.isFlippedVertically)
        assertFalse(flipped.isFlippedHorizontally)
    }

    @Test
    fun `GIVEN the original photo WHEN flipping it vertically THEN scales negatively on the mirrored axis only`() {
        // Given
        val original = original()

        // When
        val flipped = original.flipVertically()

        // Then
        assertEquals(1f, flipped.horizontalScale)
        assertEquals(-1f, flipped.verticalScale)
    }

    private fun original(): PhotoOrientation = PhotoOrientation(
        rotationDegrees = PhotoOrientation.NO_ROTATION,
        isFlippedHorizontally = false,
        isFlippedVertically = false,
    )
}
