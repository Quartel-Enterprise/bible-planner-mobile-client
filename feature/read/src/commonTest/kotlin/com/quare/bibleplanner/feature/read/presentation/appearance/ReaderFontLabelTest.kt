package com.quare.bibleplanner.feature.read.presentation.appearance

import com.quare.bibleplanner.ui.theme.font.ReaderFont
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReaderFontLabelTest {
    @Test
    fun `GIVEN every reader font WHEN reading its label THEN each font has a label of its own`() {
        // Given
        val fonts = ReaderFont.entries

        // When
        val labels = fonts.map { font -> font.labelResource.key }

        // Then
        assertEquals(
            expected = fonts.size,
            actual = labels.distinct().size,
        )
    }
}
