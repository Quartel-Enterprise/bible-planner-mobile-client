package com.quare.bibleplanner.core.utils.locale

import kotlin.test.Test
import kotlin.test.assertEquals

internal class LanguageTest {
    @Test
    fun `GIVEN every language WHEN checking for Brazilian Portuguese THEN only that language matches`() {
        // When
        val flags = Language.entries.map { it.isPortugueseBrazil }

        // Then
        assertEquals(Language.entries.map { it == Language.PORTUGUESE_BRAZIL }, flags)
        assertEquals(1, flags.count { it })
    }
}
