package com.quare.bibleplanner.core.preferences.themeselection.domain.repository

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import kotlinx.coroutines.flow.Flow

interface ThemeSelectionRepository {
    fun getThemeFlow(): Flow<Theme>

    suspend fun setTheme(theme: Theme)

    fun getContrastTypeFlow(): Flow<ContrastType>

    suspend fun setContrastType(contrastType: ContrastType)

    fun getThemeSyncEnabledFlow(): Flow<Boolean>

    // Why: enabling sync mirrors this device's theme and contrast as authoritative values so
    // they propagate to the other devices.
    suspend fun setThemeSyncEnabled(enabled: Boolean)

    fun observeSyncedTheme(): Flow<Theme?>

    fun observeSyncedContrast(): Flow<ContrastType?>

    // Why: writes locally without re-pushing, to avoid a sync echo loop.
    suspend fun applySyncedTheme(theme: Theme)

    // Why: writes locally without re-pushing, to avoid a sync echo loop.
    suspend fun applySyncedContrast(contrastType: ContrastType)
}
