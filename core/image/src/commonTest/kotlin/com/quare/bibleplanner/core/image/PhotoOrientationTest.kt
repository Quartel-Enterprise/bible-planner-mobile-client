package com.quare.bibleplanner.core.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhotoOrientationTest {
    @Test
    fun `returns to the original photo after four quarter turns`() {
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
    fun `reports being turned sideways only on quarter turns`() {
        // When
        val quarter = original().rotateQuarterTurn()
        val half = quarter.rotateQuarterTurn()

        // Then
        assertTrue(quarter.isQuarterTurned)
        assertFalse(half.isQuarterTurned)
    }

    @Test
    fun `undoes a mirroring when the same flip is applied twice`() {
        // When
        val flipped = original().flipHorizontally().flipHorizontally()

        // Then
        assertTrue(flipped.isOriginal)
    }

    @Test
    fun `mirrors along the screen axis when the photo is turned sideways`() {
        // Given — turned sideways, the photo axes are swapped on screen
        val sideways = original().rotateQuarterTurn()

        // When
        val flipped = sideways.flipHorizontally()

        // Then — flipping the photo vertically is what mirrors it horizontally on screen
        assertTrue(flipped.isFlippedVertically)
        assertFalse(flipped.isFlippedHorizontally)
    }

    @Test
    fun `scales negatively on the mirrored axis only`() {
        // When
        val flipped = original().flipVertically()

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
