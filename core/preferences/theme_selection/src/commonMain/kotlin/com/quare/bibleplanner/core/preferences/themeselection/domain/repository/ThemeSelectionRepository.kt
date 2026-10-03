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

    suspend fun setThemeSyncEnabled(enabled: Boolean)

    fun observeSyncedTheme(): Flow<Theme?>

    fun observeSyncedContrast(): Flow<ContrastType?>

    suspend fun applySyncedTheme(theme: Theme)

    suspend fun applySyncedContrast(contrastType: ContrastType)
}
