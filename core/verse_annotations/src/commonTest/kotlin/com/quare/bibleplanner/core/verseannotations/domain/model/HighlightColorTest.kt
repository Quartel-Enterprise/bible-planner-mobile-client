package com.quare.bibleplanner.core.verseannotations.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HighlightColorTest {
    @Test
    fun `builds the custom key from the hue and the lightness`() {
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
    fun `parses a preset key back into its color`() {
        // When
        val color = HighlightColor.fromKey("yellow")

        // Then
        assertEquals(
            expected = HighlightColor.Preset(PresetHighlightColor.YELLOW),
            actual = color,
        )
    }

    @Test
    fun `parses a custom key back into its components`() {
        // When
        val color = HighlightColor.fromKey("c:120:45")

        // Then
        assertEquals(
            expected = HighlightColor.Custom(hue = 120, lightness = 45),
            actual = color,
        )
    }

    @Test
    fun `returns null for a key it does not recognize`() {
        // When
        val color = HighlightColor.fromKey("c:not-a-hue:45")

        // Then
        assertNull(color)
    }
}
