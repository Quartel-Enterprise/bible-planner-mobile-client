package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.ObserveThemeSync
import kotlinx.coroutines.flow.combine

internal class ObserveThemeSyncUseCase(
    private val repository: ThemeSelectionRepository,
) : ObserveThemeSync {
    override suspend fun invoke() {
        combine(
            repository.getThemeSyncEnabledFlow(),
            repository.observeSyncedTheme(),
            repository.observeSyncedContrast(),
        ) { enabled, theme, contrast ->
            SyncedThemeSnapshot(
                isEnabled = enabled,
                theme = theme,
                contrast = contrast,
            )
        }.collect { snapshot ->
            if (snapshot.isEnabled) {
                snapshot.theme?.let { repository.applySyncedTheme(it) }
                snapshot.contrast?.let { repository.applySyncedContrast(it) }
            }
        }
    }

    private data class SyncedThemeSnapshot(
        val isEnabled: Boolean,
        val theme: Theme?,
        val contrast: ContrastType?,
    )
}
