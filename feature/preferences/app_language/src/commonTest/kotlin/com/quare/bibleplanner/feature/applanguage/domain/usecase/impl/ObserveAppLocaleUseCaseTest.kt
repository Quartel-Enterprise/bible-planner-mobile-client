package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveAppLocaleUseCaseTest {
    @Test
    fun `GIVEN the app language changes WHEN observing it THEN applies every language in order`() = runTest {
        // Given
        val appliedLanguages = mutableListOf<Language>()
        val useCase = ObserveAppLocaleUseCase(
            getAppLanguageFlow = { flowOf(Language.ENGLISH, Language.PORTUGUESE_BRAZIL) },
            applyLocale = { language -> appliedLanguages += language },
        )

        // When
        useCase()

        // Then
        assertEquals(listOf(Language.ENGLISH, Language.PORTUGUESE_BRAZIL), appliedLanguages)
    }
}
