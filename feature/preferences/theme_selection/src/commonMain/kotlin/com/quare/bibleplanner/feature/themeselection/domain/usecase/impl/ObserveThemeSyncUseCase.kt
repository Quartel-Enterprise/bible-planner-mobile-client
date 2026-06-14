package com.quare.bibleplanner.feature.themeselection.domain.usecase.impl

import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.feature.themeselection.domain.usecase.ObserveThemeSync
import com.quare.bibleplanner.ui.theme.model.ContrastType
import com.quare.bibleplanner.ui.theme.model.Theme
import kotlinx.coroutines.flow.combine

/**
 * App-scoped collector that applies synced theme + contrast into the device-local store while the
 * sync flag is on. Writing through `applySynced*` (DataStore-only) means an inbound change does not
 * re-trigger an outbound push, so there is no echo loop.
 */
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
