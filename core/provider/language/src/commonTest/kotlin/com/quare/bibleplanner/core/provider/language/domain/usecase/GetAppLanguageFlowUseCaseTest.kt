package com.quare.bibleplanner.core.provider.language.domain.usecase

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetAppLanguageFlowUseCaseTest {
    private lateinit var useCase: GetAppLanguageFlowUseCase

    @Test
    fun `GIVEN a chosen language WHEN observing the app language THEN emits it`() = runTest {
        // When
        val language = useCase().first()

        // Then
        assertEquals(Language.PORTUGUESE_BRAZIL, language)
    }

    @BeforeTest
    fun setUp() {
        useCase = GetAppLanguageFlowUseCase(FixedLanguageRepository(Language.PORTUGUESE_BRAZIL))
    }
}

private class FixedLanguageRepository(
    private val language: Language,
) : AppLanguageRepository {
    override fun getLanguageFlow(): Flow<Language> = flowOf(language)

    override suspend fun setLanguage(language: Language) = error("unused")

    override fun getLanguageSyncEnabledFlow(): Flow<Boolean> = error("unused")

    override suspend fun setLanguageSyncEnabled(enabled: Boolean) = error("unused")

    override fun observeSyncedLanguage(): Flow<Language?> = error("unused")

    override suspend fun applySyncedLanguage(language: Language) = error("unused")
}
