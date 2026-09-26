package com.quare.bibleplanner.core.provider.language.data.mapper

import com.quare.bibleplanner.core.provider.language.fake.FixedLanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AppLanguageMapperTest {
    private lateinit var mapper: AppLanguageMapper

    @Test
    fun `GIVEN every language WHEN storing and reading it back THEN returns the same language`() {
        // Given
        val languages = Language.entries

        // When
        val roundTripped = languages.map { language ->
            mapper.mapPreferenceToModel(mapper.mapModelToPreference(language))
        }

        // Then
        assertEquals(languages, roundTripped)
    }

    @Test
    fun `GIVEN every language WHEN storing it THEN uses its locale tag`() {
        // When
        val tags = Language.entries.associateWith(mapper::mapModelToPreference)

        // Then
        assertEquals(
            mapOf(
                Language.ENGLISH to "en",
                Language.PORTUGUESE_BRAZIL to "pt-BR",
                Language.SPANISH to "es",
            ),
            tags,
        )
    }

    @Test
    fun `GIVEN no stored language WHEN reading it THEN falls back to the app language`() {
        // When
        val language = mapper.mapPreferenceToModel(null)

        // Then
        assertEquals(Language.SPANISH, language)
    }

    @Test
    fun `GIVEN an unknown stored tag WHEN reading it THEN falls back to the app language`() {
        // When
        val language = mapper.mapPreferenceToModel("fr")

        // Then
        assertEquals(Language.SPANISH, language)
    }

    @BeforeTest
    fun setUp() {
        mapper = AppLanguageMapper(FixedLanguageProvider(Language.SPANISH))
    }
}
