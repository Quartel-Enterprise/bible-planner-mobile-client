package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.login.presentation.factory.DesktopAuthSuccessHtmlFactory
import com.quare.bibleplanner.feature.login.presentation.mapper.LanguageToDesktopAuthSuccessStringsMapper
import com.quare.bibleplanner.core.model.theme.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetDesktopAuthSuccessHtmlFlowTest {
    private lateinit var getDesktopAuthSuccessHtmlFlow: GetDesktopAuthSuccessHtmlFlow
    private lateinit var language: MutableStateFlow<Language>

    @Test
    fun `GIVEN the page is rendered WHEN the language changes THEN renders it again in the new language`() = runTest {
        // Given
        prepareScenario()
        val pages = mutableListOf<String>()
        val collection = launch {
            getDesktopAuthSuccessHtmlFlow().take(2).toList().mapTo(pages) { it.getOrThrow() }
        }
        runCurrent()

        // When
        language.value = Language.PORTUGUESE_BRAZIL
        collection.join()

        // Then
        assertEquals(2, pages.size)
        assertTrue(pages[0].contains("Signed in!"))
        assertTrue(pages[1].contains("Login concluído!"))
    }

    private fun prepareScenario() {
        language = MutableStateFlow(Language.ENGLISH)
        getDesktopAuthSuccessHtmlFlow = GetDesktopAuthSuccessHtmlFlow(
            getThemeOptionFlow = { MutableStateFlow(Theme.DARK) },
            getAppLanguageFlow = { language },
            desktopAuthSuccessHtmlFactory = DesktopAuthSuccessHtmlFactory(
                getResourcesAsTextResult = GetResourcesAsTextResult(),
                languageToDesktopAuthSuccessStringsMapper = LanguageToDesktopAuthSuccessStringsMapper(),
            ),
        )
    }
}
