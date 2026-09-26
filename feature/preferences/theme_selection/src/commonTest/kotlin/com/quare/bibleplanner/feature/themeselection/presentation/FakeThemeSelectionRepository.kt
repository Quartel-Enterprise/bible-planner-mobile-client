package com.quare.bibleplanner.feature.themeselection.presentation

import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeThemeSelectionRepository(
    initialTheme: Theme,
    initialContrast: ContrastType,
    initialSyncEnabled: Boolean,
) : ThemeSelectionRepository {
    val theme = MutableStateFlow(initialTheme)
    val contrast = MutableStateFlow(initialContrast)
    val isSyncEnabled = MutableStateFlow(initialSyncEnabled)

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
        isSyncEnabled.value = enabled
    }

    override fun observeSyncedTheme(): Flow<Theme?> = error("unused")

    override fun observeSyncedContrast(): Flow<ContrastType?> = error("unused")

    override suspend fun applySyncedTheme(theme: Theme) = error("unused")

    override suspend fun applySyncedContrast(contrastType: ContrastType) = error("unused")
}
