package com.quare.bibleplanner.feature.bibleversion.presentation.factory

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetBibleVersionsByLanguageUseCase
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.bibleversion.fake.bibleModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionsUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BibleVersionsUiStateFactoryTest {
    @Test
    fun `shows the versions grouped by language`() = runTest {
        // Given
        val acf = bibleModel("ACF")
        val factory = createFactory(FakeBibleRepository(listOf(acf)))

        // When
        val state = factory.create().first()

        // Then
        assertEquals(
            expected = BibleVersionsUiState.Success(mapOf(Language.PORTUGUESE_BRAZIL to listOf(acf))),
            actual = state,
        )
    }

    @Test
    fun `shows an error when no version is available`() = runTest {
        // Given
        val factory = createFactory(FakeBibleRepository(emptyList()))

        // When
        val state = factory.create().first()

        // Then
        assertEquals(
            expected = BibleVersionsUiState.Error,
            actual = state,
        )
    }

    private fun createFactory(repository: FakeBibleRepository): BibleVersionsUiStateFactory =
        BibleVersionsUiStateFactory(
            GetBibleVersionsByLanguageUseCase(
                repository = repository,
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
            ),
        )
}
