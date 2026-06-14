package com.quare.bibleplanner.feature.themeselection.domain.repository

import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow

interface ThemeSelectionRepository {
    fun getThemeFlow(): Flow<Theme>

    suspend fun setTheme(theme: Theme)

    fun getContrastTypeFlow(): Flow<ContrastType>

    suspend fun setContrastType(contrastType: ContrastType)

    /** Account-global flag controlling whether theme + contrast sync across devices. */
    fun getThemeSyncEnabledFlow(): Flow<Boolean>

    /**
     * Persists the sync flag (synced across devices). When [enabled] is true the current theme and
     * contrast are mirrored as the authoritative values so they propagate to the other devices.
     */
    suspend fun setThemeSyncEnabled(enabled: Boolean)

    /** Synced theme coming from another device, or null when no synced value exists yet. */
    fun observeSyncedTheme(): Flow<Theme?>

    /** Synced contrast coming from another device, or null when no synced value exists yet. */
    fun observeSyncedContrast(): Flow<ContrastType?>

    /** Writes a remote theme into the device-local store without re-pushing it. */
    suspend fun applySyncedTheme(theme: Theme)

    /** Writes a remote contrast into the device-local store without re-pushing it. */
    suspend fun applySyncedContrast(contrastType: ContrastType)
}
