package com.quare.bibleplanner.feature.applanguage.presentation

import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InitAppLocaleTest {
    @Test
    fun `GIVEN a stored app language WHEN initializing the locale THEN applies only the current language`() = runTest {
        // Given
        val appliedLanguages = mutableListOf<Language>()

        // When
        initAppLocale(
            getAppLanguageFlow = { flowOf(Language.SPANISH, Language.ENGLISH) },
            applyLocale = { language -> appliedLanguages += language },
        )

        // Then
        assertEquals(listOf(Language.SPANISH), appliedLanguages)
    }
}
