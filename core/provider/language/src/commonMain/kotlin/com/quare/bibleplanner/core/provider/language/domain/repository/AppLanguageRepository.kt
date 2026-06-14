package com.quare.bibleplanner.core.provider.language.domain.repository

import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow

interface AppLanguageRepository {
    fun getLanguageFlow(): Flow<Language>

    suspend fun setLanguage(language: Language)

    /** Account-global flag controlling whether the app language syncs across devices. */
    fun getLanguageSyncEnabledFlow(): Flow<Boolean>

    /**
     * Persists the sync flag (synced across devices). When [enabled] is true the current language is
     * mirrored as the authoritative value so it propagates to the other devices.
     */
    suspend fun setLanguageSyncEnabled(enabled: Boolean)

    /** Synced language coming from another device, or null when no synced value exists yet. */
    fun observeSyncedLanguage(): Flow<Language?>

    /** Writes a remote language into the device-local store without re-pushing it. */
    suspend fun applySyncedLanguage(language: Language)
}
