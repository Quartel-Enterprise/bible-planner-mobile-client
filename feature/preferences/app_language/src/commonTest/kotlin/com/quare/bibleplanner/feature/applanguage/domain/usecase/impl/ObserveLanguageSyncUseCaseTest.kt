package com.quare.bibleplanner.feature.applanguage.domain.usecase.impl

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ObserveLanguageSyncUseCaseTest {
    @Test
    fun `GIVEN sync enabled and a synced language WHEN observing THEN applies the synced language`() = runTest {
        // Given
        val repository = FakeAppLanguageRepository(
            syncEnabled = true,
            syncedLanguage = Language.SPANISH,
        )
        val useCase = ObserveLanguageSyncUseCase(repository)

        // When
        useCase()

        // Then
        assertEquals(Language.SPANISH, repository.appliedLanguage)
    }

    @Test
    fun `GIVEN sync disabled WHEN observing THEN applies nothing`() = runTest {
        // Given
        val repository = FakeAppLanguageRepository(
            syncEnabled = false,
            syncedLanguage = Language.SPANISH,
        )
        val useCase = ObserveLanguageSyncUseCase(repository)

        // When
        useCase()

        // Then
        assertNull(repository.appliedLanguage)
    }

    @Test
    fun `GIVEN a missing synced language WHEN observing THEN applies nothing`() = runTest {
        // Given
        val repository = FakeAppLanguageRepository(
            syncEnabled = true,
            syncedLanguage = null,
        )
        val useCase = ObserveLanguageSyncUseCase(repository)

        // When
        useCase()

        // Then
        assertNull(repository.appliedLanguage)
    }

    private class FakeAppLanguageRepository(
        private val syncEnabled: Boolean,
        private val syncedLanguage: Language?,
    ) : AppLanguageRepository {
        var appliedLanguage: Language? = null

        override fun getLanguageSyncEnabledFlow(): Flow<Boolean> = flowOf(syncEnabled)

        override fun observeSyncedLanguage(): Flow<Language?> = flowOf(syncedLanguage)

        override suspend fun applySyncedLanguage(language: Language) {
            appliedLanguage = language
        }

        override fun getLanguageFlow(): Flow<Language> = flowOf(Language.ENGLISH)

        override suspend fun setLanguage(language: Language) {}

        override suspend fun setLanguageSyncEnabled(enabled: Boolean) {}
    }
}
