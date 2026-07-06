package com.quare.bibleplanner.feature.daystudy.domain.mapper

import com.quare.bibleplanner.core.utils.locale.Language
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LanguageCodeMapperTest {
    private val mapper = LanguageCodeMapper()

    @Test
    fun `GIVEN portuguese brazil WHEN mapping THEN returns pt-BR`() {
        // Given
        val language = Language.PORTUGUESE_BRAZIL

        // When
        val code = mapper.map(language)

        // Then
        assertEquals("pt-BR", code)
    }

    @Test
    fun `GIVEN english WHEN mapping THEN returns en`() {
        // Given
        val language = Language.ENGLISH

        // When
        val code = mapper.map(language)

        // Then
        assertEquals("en", code)
    }

    @Test
    fun `GIVEN spanish WHEN mapping THEN returns es`() {
        // Given
        val language = Language.SPANISH

        // When
        val code = mapper.map(language)

        // Then
        assertEquals("es", code)
    }
}
