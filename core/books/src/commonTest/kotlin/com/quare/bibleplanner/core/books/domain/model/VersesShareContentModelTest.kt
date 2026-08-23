package com.quare.bibleplanner.core.books.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VersesShareContentModelTest {
    @Test
    fun `heads the passage with its reference and the version's short name`() {
        // Given
        val content = VersesShareContentModel(
            text = "[1] No princípio, Deus criou os céus e a terra.\n[2] A terra era sem forma e vazia.",
            reference = "Gênesis 1:1-2",
            versionAbbreviation = "A21",
        )

        // Then
        assertEquals(
            expected = "Gênesis 1:1-2 A21\n" +
                "[1] No princípio, Deus criou os céus e a terra.\n" +
                "[2] A terra era sem forma e vazia.",
            actual = content.shareText,
        )
    }
}
