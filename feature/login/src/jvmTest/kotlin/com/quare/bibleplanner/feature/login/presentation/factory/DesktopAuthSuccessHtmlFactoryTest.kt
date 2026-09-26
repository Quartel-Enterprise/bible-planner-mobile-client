package com.quare.bibleplanner.feature.login.presentation.factory

import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.login.presentation.GetResourcesAsTextResult
import com.quare.bibleplanner.feature.login.presentation.mapper.LanguageToDesktopAuthSuccessStringsMapper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DesktopAuthSuccessHtmlFactoryTest {
    private lateinit var factory: DesktopAuthSuccessHtmlFactory

    @BeforeTest
    fun setUp() {
        factory = DesktopAuthSuccessHtmlFactory(
            getResourcesAsTextResult = GetResourcesAsTextResult(),
            languageToDesktopAuthSuccessStringsMapper = LanguageToDesktopAuthSuccessStringsMapper(),
        )
    }

    @Test
    fun `GIVEN a language WHEN creating the page THEN fills every placeholder with its strings`() {
        // When
        val html = factory
            .create(
                theme = Theme.LIGHT,
                language = Language.SPANISH,
            ).getOrThrow()

        // Then
        assertTrue(html.contains("<title>Bible Planner</title>"))
        assertTrue(html.contains("¡Sesión iniciada!"))
        assertTrue(html.contains("Puedes cerrar esta pestaña y volver a Bible Planner."))
        assertFalse(html.contains("{{"))
    }

    @Test
    fun `GIVEN the light theme WHEN creating the page THEN inlines only the light colors`() {
        // When
        val html = factory
            .create(
                theme = Theme.LIGHT,
                language = Language.ENGLISH,
            ).getOrThrow()

        // Then
        assertTrue(html.contains("color-scheme: light"))
        assertFalse(html.contains("color-scheme: dark"))
        assertFalse(html.contains("@media (prefers-color-scheme"))
    }

    @Test
    fun `GIVEN the dark theme WHEN creating the page THEN inlines only the dark colors`() {
        // When
        val html = factory
            .create(
                theme = Theme.DARK,
                language = Language.ENGLISH,
            ).getOrThrow()

        // Then
        assertTrue(html.contains("color-scheme: dark"))
        assertFalse(html.contains("color-scheme: light"))
    }

    @Test
    fun `GIVEN the system theme WHEN creating the page THEN lets the browser pick between both palettes`() {
        // When
        val html = factory
            .create(
                theme = Theme.SYSTEM,
                language = Language.ENGLISH,
            ).getOrThrow()

        // Then
        assertTrue(html.contains("@media (prefers-color-scheme: light) {"))
        assertTrue(html.contains("@media (prefers-color-scheme: dark) {"))
        assertTrue(html.contains("color-scheme: light"))
        assertTrue(html.contains("color-scheme: dark"))
    }
}
