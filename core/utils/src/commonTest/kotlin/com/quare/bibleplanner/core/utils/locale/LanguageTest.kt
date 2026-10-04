package com.quare.bibleplanner.core.utils.locale

import kotlin.test.Test
import kotlin.test.assertEquals

internal class LanguageTest {
    @Test
    fun `GIVEN every language WHEN checking for Brazilian Portuguese THEN only that language matches`() {
        // Given
        val languages = Language.entries

        // When
        val flags = languages.map { it.isPortugueseBrazil }

        // Then
        assertEquals(languages.map { it == Language.PORTUGUESE_BRAZIL }, flags)
        assertEquals(1, flags.count { it })
    }
}
