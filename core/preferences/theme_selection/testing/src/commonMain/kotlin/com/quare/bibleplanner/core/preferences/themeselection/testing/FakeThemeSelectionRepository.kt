package com.quare.bibleplanner.core.preferences.themeselection.testing

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.domain.repository.ThemeSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeThemeSelectionRepository(
    initialTheme: Theme,
    initialContrast: ContrastType,
    initialSyncEnabled: Boolean,
) : ThemeSelectionRepository {
    val theme = MutableStateFlow(initialTheme)
    val contrast = MutableStateFlow(initialContrast)
    val isSyncEnabled = MutableStateFlow(initialSyncEnabled)
    val syncedTheme = MutableStateFlow<Theme?>(null)
    val syncedContrast = MutableStateFlow<ContrastType?>(null)
    val syncEnabledWrites = mutableListOf<Boolean>()
    val appliedThemes = mutableListOf<Theme>()
    val appliedContrasts = mutableListOf<ContrastType>()

    override fun getThemeFlow(): Flow<Theme> = theme

    override suspend fun setTheme(theme: Theme) {
        this.theme.value = theme
    }

    override fun getContrastTypeFlow(): Flow<ContrastType> = contrast

    override suspend fun setContrastType(contrastType: ContrastType) {
        contrast.value = contrastType
    }

    override fun getThemeSyncEnabledFlow(): Flow<Boolean> = isSyncEnabled

    override suspend fun setThemeSyncEnabled(enabled: Boolean) {
        syncEnabledWrites += enabled
        isSyncEnabled.value = enabled
    }

    override fun observeSyncedTheme(): Flow<Theme?> = syncedTheme

    override fun observeSyncedContrast(): Flow<ContrastType?> = syncedContrast

    override suspend fun applySyncedTheme(theme: Theme) {
        appliedThemes += theme
        this.theme.value = theme
    }

    override suspend fun applySyncedContrast(contrastType: ContrastType) {
        appliedContrasts += contrastType
        contrast.value = contrastType
    }
}
