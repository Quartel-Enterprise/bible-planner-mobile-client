package com.quare.bibleplanner.core.verseannotations.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HighlightColorTest {
    @Test
    fun `GIVEN a custom color WHEN reading its key THEN builds it from the hue and the lightness`() {
        // Given
        val color = HighlightColor.Custom(
            hue = 265,
            lightness = 62,
        )

        // When
        val key = color.key

        // Then
        assertEquals(
            expected = "c:265:62",
            actual = key,
        )
    }

    @Test
    fun `GIVEN a preset key WHEN parsing it THEN returns its preset color`() {
        // Given
        val key = "yellow"

        // When
        val color = HighlightColor.fromKey(key)

        // Then
        assertEquals(
            expected = HighlightColor.Preset(PresetHighlightColor.YELLOW),
            actual = color,
        )
    }

    @Test
    fun `GIVEN a custom key WHEN parsing it THEN returns a custom color with its components`() {
        // Given
        val key = "c:120:45"

        // When
        val color = HighlightColor.fromKey(key)

        // Then
        assertEquals(
            expected = HighlightColor.Custom(hue = 120, lightness = 45),
            actual = color,
        )
    }

    @Test
    fun `GIVEN an unrecognized key WHEN parsing it THEN returns null`() {
        // Given
        val key = "c:not-a-hue:45"

        // When
        val color = HighlightColor.fromKey(key)

        // Then
        assertNull(color)
    }
}
