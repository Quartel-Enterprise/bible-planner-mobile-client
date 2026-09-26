package com.quare.bibleplanner.feature.applanguage.presentation

import com.quare.bibleplanner.core.provider.language.domain.repository.AppLanguageRepository
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeAppLanguageRepository(
    initialLanguage: Language,
    initialSyncEnabled: Boolean,
) : AppLanguageRepository {
    val language = MutableStateFlow(initialLanguage)
    val isSyncEnabled = MutableStateFlow(initialSyncEnabled)

    override fun getLanguageFlow(): Flow<Language> = language

    override suspend fun setLanguage(language: Language) {
        this.language.value = language
    }

    override fun getLanguageSyncEnabledFlow(): Flow<Boolean> = isSyncEnabled

    override suspend fun setLanguageSyncEnabled(enabled: Boolean) {
        isSyncEnabled.value = enabled
    }

    override fun observeSyncedLanguage(): Flow<Language?> = error("unused")

    override suspend fun applySyncedLanguage(language: Language) = error("unused")
}
