package com.quare.bibleplanner.feature.materialyou.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.feature.materialyou.domain.repository.MaterialYouRepository
import com.quare.bibleplanner.feature.materialyou.domain.usecase.IsDynamicColorSupported
import com.quare.bibleplanner.feature.materialyou.domain.usecase.SetIsDynamicColorsEnabled
import kotlinx.coroutines.flow.first

/**
 * Writes the dynamic-colors preference to the device-local store and, when theme sync is on and the OS
 * supports Material You, mirrors it into the synced store so it propagates to the user's other devices.
 */
internal class SetIsDynamicColorsEnabledUseCase(
    private val repository: MaterialYouRepository,
    private val isDynamicColorSupported: IsDynamicColorSupported,
    private val syncedPreferenceDao: SyncedPreferenceDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : SetIsDynamicColorsEnabled {
    override suspend fun invoke(isEnabled: Boolean) {
        repository.setIsDynamicColorsEnabled(isEnabled)
        if (isDynamicColorSupported() && isThemeSyncEnabled()) {
            syncedPreferenceDao.setLocal(
                key = SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED,
                value = isEnabled.toString(),
                updatedAt = currentTimestampProvider.getCurrentTimestamp(),
            )
        }
    }

    private suspend fun isThemeSyncEnabled(): Boolean =
        syncedPreferenceDao.observeValue(SyncedPreferenceKeys.THEME_SYNC_ENABLED).first().toBoolean()
}
