package com.quare.bibleplanner.core.provider.platform.domain.usecase

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.locale.Language
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetAppStoreLinkUseCaseTest {
    private lateinit var useCase: GetAppStoreLinkUseCase

    @Test
    fun `GIVEN an iPhone in each language WHEN getting the store link THEN points to the matching App Store`() {
        // When
        val links = Language.entries.associateWith { language ->
            prepareScenario(
                platform = Platform.Ios,
                language = language,
            )
            useCase()
        }

        // Then
        assertEquals(
            mapOf(
                Language.ENGLISH to "https://apps.apple.com/us/app/bible-planner-reading-plans/id6756151777",
                Language.PORTUGUESE_BRAZIL to "https://apps.apple.com/br/app/bible-planner-reading-plans/id6756151777",
                Language.SPANISH to "https://apps.apple.com/es/app/bible-planner-reading-plans/id6756151777",
            ),
            links,
        )
    }

    @Test
    fun `GIVEN an Android phone in each language WHEN getting the store link THEN opens Google Play in it`() {
        // When
        val links = Language.entries.associateWith { language ->
            prepareScenario(
                platform = Platform.Android,
                language = language,
            )
            useCase()
        }

        // Then
        assertEquals(
            mapOf(
                Language.ENGLISH to "https://play.google.com/store/apps/details?id=com.quare.bibleplanner&hl=en",
                Language.PORTUGUESE_BRAZIL to
                    "https://play.google.com/store/apps/details?id=com.quare.bibleplanner&hl=pt-BR",
                Language.SPANISH to "https://play.google.com/store/apps/details?id=com.quare.bibleplanner&hl=es",
            ),
            links,
        )
    }

    @Test
    fun `GIVEN a desktop WHEN getting the store link THEN points to the website`() {
        // Given
        prepareScenario(
            platform = Platform.Desktop.Windows,
            language = Language.ENGLISH,
        )

        // When
        val link = useCase()

        // Then
        assertEquals("https://bibleplanner.app", link)
    }

    private fun prepareScenario(
        platform: Platform,
        language: Language,
    ) {
        useCase = GetAppStoreLinkUseCase(
            platform = platform,
            languageProvider = FixedLanguageProvider(language),
        )
    }
}

private class FixedLanguageProvider(
    private val appLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("unused")

    override fun getAppLanguage(): Language = appLanguage
}
