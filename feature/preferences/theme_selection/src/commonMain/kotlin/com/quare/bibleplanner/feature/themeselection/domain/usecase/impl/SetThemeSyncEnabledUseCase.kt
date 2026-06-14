package com.quare.bibleplanner.feature.themeselection.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.feature.materialyou.domain.usecase.GetIsDynamicColorsEnabledFlow
import com.quare.bibleplanner.feature.materialyou.domain.usecase.IsDynamicColorSupported
import com.quare.bibleplanner.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.quare.bibleplanner.feature.themeselection.domain.usecase.SetThemeSyncEnabled
import kotlinx.coroutines.flow.first

/**
 * Toggles the account-global theme sync flag. The repository handles the flag plus the theme/contrast
 * snapshot; dynamic colors are mirrored here too (only on a device where Material You is supported) so
 * the whole theme appearance becomes authoritative when sync is enabled.
 */
internal class SetThemeSyncEnabledUseCase(
    private val repository: ThemeSelectionRepository,
    private val getIsDynamicColorsEnabledFlow: GetIsDynamicColorsEnabledFlow,
    private val isDynamicColorSupported: IsDynamicColorSupported,
    private val syncedPreferenceDao: SyncedPreferenceDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : SetThemeSyncEnabled {
    override suspend fun invoke(enabled: Boolean) {
        repository.setThemeSyncEnabled(enabled)
        if (enabled && isDynamicColorSupported()) {
            syncedPreferenceDao.setLocal(
                key = SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED,
                value = getIsDynamicColorsEnabledFlow().first().toString(),
                updatedAt = currentTimestampProvider.getCurrentTimestamp(),
            )
        }
    }
}
