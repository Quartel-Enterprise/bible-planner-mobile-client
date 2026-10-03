package com.quare.bibleplanner.core.provider.language.domain.repository

import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow

interface AppLanguageRepository {
    fun getLanguageFlow(): Flow<Language>

    suspend fun setLanguage(language: Language)

    fun getLanguageSyncEnabledFlow(): Flow<Boolean>

    // Why: enabling sync mirrors this device's language as the authoritative value so it
    // propagates to the other devices.
    suspend fun setLanguageSyncEnabled(enabled: Boolean)

    fun observeSyncedLanguage(): Flow<Language?>

    // Why: writes locally without re-pushing, to avoid a sync echo loop.
    suspend fun applySyncedLanguage(language: Language)
}
