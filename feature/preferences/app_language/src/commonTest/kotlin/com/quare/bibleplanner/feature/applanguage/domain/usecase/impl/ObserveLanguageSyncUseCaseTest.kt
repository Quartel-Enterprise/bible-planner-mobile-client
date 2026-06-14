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
    fun `applies synced language when sync is enabled`() = runTest {
        val repository = FakeAppLanguageRepository(
            syncEnabled = true,
            syncedLanguage = Language.SPANISH,
        )

        ObserveLanguageSyncUseCase(repository).invoke()

        assertEquals(Language.SPANISH, repository.appliedLanguage)
    }

    @Test
    fun `applies nothing when sync is disabled`() = runTest {
        val repository = FakeAppLanguageRepository(
            syncEnabled = false,
            syncedLanguage = Language.SPANISH,
        )

        ObserveLanguageSyncUseCase(repository).invoke()

        assertNull(repository.appliedLanguage)
    }

    @Test
    fun `skips a missing synced language`() = runTest {
        val repository = FakeAppLanguageRepository(
            syncEnabled = true,
            syncedLanguage = null,
        )

        ObserveLanguageSyncUseCase(repository).invoke()

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

        override suspend fun setLanguage(language: Language) = Unit

        override suspend fun setLanguageSyncEnabled(enabled: Boolean) = Unit
    }
}
