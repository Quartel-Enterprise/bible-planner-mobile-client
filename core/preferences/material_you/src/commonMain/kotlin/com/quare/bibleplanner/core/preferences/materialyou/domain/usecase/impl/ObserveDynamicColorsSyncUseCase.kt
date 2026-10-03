package com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.materialyou.domain.repository.MaterialYouRepository
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.IsDynamicColorSupported
import com.quare.bibleplanner.core.preferences.materialyou.domain.usecase.ObserveDynamicColorsSync
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import kotlinx.coroutines.flow.combine

internal class ObserveDynamicColorsSyncUseCase(
    private val repository: MaterialYouRepository,
    private val isDynamicColorSupported: IsDynamicColorSupported,
    private val syncedPreferenceDao: SyncedPreferenceDao,
) : ObserveDynamicColorsSync {
    override suspend fun invoke() {
        if (!isDynamicColorSupported()) return
        combine(
            syncedPreferenceDao.observeValue(SyncedPreferenceKeys.THEME_SYNC_ENABLED),
            syncedPreferenceDao.observeValue(SyncedPreferenceKeys.DYNAMIC_COLORS_ENABLED),
        ) { syncEnabled, dynamicColors ->
            syncEnabled.toBoolean() to dynamicColors
        }.collect { (isSyncEnabled, dynamicColors) ->
            if (isSyncEnabled && dynamicColors != null) {
                repository.setIsDynamicColorsEnabled(dynamicColors.toBoolean())
            }
        }
    }
}
