package com.quare.bibleplanner.core.provider.platform.domain.usecase

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.locale.Language
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetAppStoreLinkUseCaseTest {
    private lateinit var useCase: GetAppStoreLinkUseCase
    private lateinit var languageProvider: MutableLanguageProvider

    @Test
    fun `GIVEN an iPhone in each language WHEN getting the store link THEN points to the matching App Store`() {
        // Given
        prepareScenario(platform = Platform.Ios)

        // When
        val links = Language.entries.associateWith { language ->
            languageProvider.language = language
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
        // Given
        prepareScenario(platform = Platform.Android)

        // When
        val links = Language.entries.associateWith { language ->
            languageProvider.language = language
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
        prepareScenario(platform = Platform.Desktop.Windows)

        // When
        val link = useCase()

        // Then
        assertEquals("https://bibleplanner.app", link)
    }

    private fun prepareScenario(platform: Platform) {
        languageProvider = MutableLanguageProvider(language = Language.ENGLISH)
        useCase = GetAppStoreLinkUseCase(
            platform = platform,
            languageProvider = languageProvider,
        )
    }
}

private class MutableLanguageProvider(
    var language: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("unused")

    override fun getAppLanguage(): Language = language
}
