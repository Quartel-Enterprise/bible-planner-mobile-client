package com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.preferences.materialyou.domain.repository.MaterialYouRepository
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.IsDynamicColorSupported
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.SetIsDynamicColorsEnabled
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import kotlinx.coroutines.flow.first

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
