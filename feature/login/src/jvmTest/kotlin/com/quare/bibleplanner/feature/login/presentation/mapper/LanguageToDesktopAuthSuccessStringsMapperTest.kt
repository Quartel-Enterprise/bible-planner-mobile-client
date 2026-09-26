package com.quare.bibleplanner.feature.login.presentation.mapper

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.login.presentation.model.DesktopAuthSuccessStrings
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LanguageToDesktopAuthSuccessStringsMapperTest {
    private lateinit var mapper: LanguageToDesktopAuthSuccessStringsMapper

    @BeforeTest
    fun setUp() {
        mapper = LanguageToDesktopAuthSuccessStringsMapper()
    }

    @Test
    fun `GIVEN english WHEN mapping THEN returns the english page strings`() {
        // When
        val strings = mapper.map(Language.ENGLISH)

        // Then
        assertEquals(
            DesktopAuthSuccessStrings(
                heading = "Signed in!",
                message = "You can close this tab and return to Bible Planner.",
                htmlLang = "en",
            ),
            strings,
        )
    }

    @Test
    fun `GIVEN brazilian portuguese WHEN mapping THEN returns the portuguese page strings`() {
        // When
        val strings = mapper.map(Language.PORTUGUESE_BRAZIL)

        // Then
        assertEquals(
            DesktopAuthSuccessStrings(
                heading = "Login concluído!",
                message = "Você pode fechar esta aba e voltar para o Bible Planner.",
                htmlLang = "pt-BR",
            ),
            strings,
        )
    }

    @Test
    fun `GIVEN spanish WHEN mapping THEN returns the spanish page strings`() {
        // When
        val strings = mapper.map(Language.SPANISH)

        // Then
        assertEquals(
            DesktopAuthSuccessStrings(
                heading = "¡Sesión iniciada!",
                message = "Puedes cerrar esta pestaña y volver a Bible Planner.",
                htmlLang = "es",
            ),
            strings,
        )
    }
}
