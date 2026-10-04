package com.quare.bibleplanner.core.verseannotations.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PresetHighlightColorTest {
    @Test
    fun `GIVEN every preset color WHEN keeping the ones that require Pro THEN only the first three are left out`() {
        // Given
        val presets = PresetHighlightColor.entries

        // When
        val proPresets = presets.filter(PresetHighlightColor::requiresPro)

        // Then
        assertEquals(
            expected = listOf(
                PresetHighlightColor.PINK,
                PresetHighlightColor.ORANGE,
                PresetHighlightColor.PURPLE,
            ),
            actual = proPresets,
        )
    }
}
