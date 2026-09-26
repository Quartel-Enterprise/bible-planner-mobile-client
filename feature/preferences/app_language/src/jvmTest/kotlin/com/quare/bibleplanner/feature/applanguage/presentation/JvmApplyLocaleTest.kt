package com.quare.bibleplanner.feature.applanguage.presentation

import com.quare.bibleplanner.core.utils.locale.Language
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JvmApplyLocaleTest {
    private val defaultLocale: Locale = Locale.getDefault()
    private lateinit var applyLocale: JvmApplyLocale

    @BeforeTest
    fun setUp() {
        applyLocale = JvmApplyLocale()
    }

    @AfterTest
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `GIVEN english WHEN applying it THEN switches the default locale to english`() {
        // When
        applyLocale(Language.ENGLISH)

        // Then
        assertEquals("en", Locale.getDefault().toLanguageTag())
    }

    @Test
    fun `GIVEN brazilian portuguese WHEN applying it THEN switches the default locale to brazilian portuguese`() {
        // When
        applyLocale(Language.PORTUGUESE_BRAZIL)

        // Then
        assertEquals("pt-BR", Locale.getDefault().toLanguageTag())
    }

    @Test
    fun `GIVEN spanish WHEN applying it THEN switches the default locale to spanish`() {
        // When
        applyLocale(Language.SPANISH)

        // Then
        assertEquals("es", Locale.getDefault().toLanguageTag())
    }
}
