package com.quare.bibleplanner.core.verseannotations.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PresetHighlightColorTest {
    @Test
    fun `only the first three presets are free`() {
        // When
        val proPresets = PresetHighlightColor.entries.filter { it.requiresPro }

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
