package com.quare.bibleplanner.core.provider.language.data

import com.quare.bibleplanner.core.utils.locale.Language
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JvmLanguageProviderTest {
    private lateinit var provider: JvmLanguageProvider
    private lateinit var originalLocale: Locale
    private var originalLanguage: String? = null
    private var originalCountry: String? = null

    @BeforeTest
    fun setUp() {
        provider = JvmLanguageProvider()
        originalLocale = Locale.getDefault()
        originalLanguage = System.getProperty(LANGUAGE_PROPERTY)
        originalCountry = System.getProperty(COUNTRY_PROPERTY)
    }

    @AfterTest
    fun tearDown() {
        Locale.setDefault(originalLocale)
        restoreProperty(
            key = LANGUAGE_PROPERTY,
            value = originalLanguage,
        )
        restoreProperty(
            key = COUNTRY_PROPERTY,
            value = originalCountry,
        )
    }

    @Test
    fun `GIVEN a Brazilian Portuguese system WHEN reading the device language THEN returns Brazilian Portuguese`() {
        // Given
        setSystemLocale(
            language = "pt",
            country = "BR",
        )

        // When
        val language = provider.getDeviceLanguage()

        // Then
        assertEquals(Language.PORTUGUESE_BRAZIL, language)
    }

    @Test
    fun `GIVEN a European Portuguese system WHEN reading the device language THEN falls back to English`() {
        // Given
        setSystemLocale(
            language = "pt",
            country = "PT",
        )

        // When
        val language = provider.getDeviceLanguage()

        // Then
        assertEquals(Language.ENGLISH, language)
    }

    @Test
    fun `GIVEN any Spanish system WHEN reading the device language THEN returns Spanish`() {
        // Given
        setSystemLocale(
            language = "es",
            country = "MX",
        )

        // When
        val language = provider.getDeviceLanguage()

        // Then
        assertEquals(Language.SPANISH, language)
    }

    @Test
    fun `GIVEN a Spanish default locale WHEN reading the app language THEN returns Spanish`() {
        // Given
        Locale.setDefault(Locale.forLanguageTag("es-AR"))

        // When
        val language = provider.getAppLanguage()

        // Then
        assertEquals(Language.SPANISH, language)
    }

    @Test
    fun `GIVEN a German default locale WHEN reading the app language THEN falls back to English`() {
        // Given
        Locale.setDefault(Locale.GERMANY)

        // When
        val language = provider.getAppLanguage()

        // Then
        assertEquals(Language.ENGLISH, language)
    }

    private fun setSystemLocale(
        language: String,
        country: String,
    ) {
        System.setProperty(LANGUAGE_PROPERTY, language)
        System.setProperty(COUNTRY_PROPERTY, country)
    }

    private fun restoreProperty(
        key: String,
        value: String?,
    ) {
        if (value == null) {
            System.clearProperty(key)
        } else {
            System.setProperty(key, value)
        }
    }

    private companion object {
        const val LANGUAGE_PROPERTY = "user.language"
        const val COUNTRY_PROPERTY = "user.country"
    }
}
