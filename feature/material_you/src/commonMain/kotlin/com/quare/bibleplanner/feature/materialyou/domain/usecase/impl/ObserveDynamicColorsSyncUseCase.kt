package com.quare.bibleplanner.feature.materialyou.domain.usecase.impl

import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceKeys
import com.quare.bibleplanner.feature.materialyou.domain.repository.MaterialYouRepository
import com.quare.bibleplanner.feature.materialyou.domain.usecase.IsDynamicColorSupported
import com.quare.bibleplanner.feature.materialyou.domain.usecase.ObserveDynamicColorsSync
import kotlinx.coroutines.flow.combine

/**
 * App-scoped collector that applies a synced dynamic-colors value into the device-local store while
 * theme sync is on. No-op on devices that do not support Material You, so an incoming value from an
 * Android device never affects an unsupported one. Writes straight through the repository (no mirror),
 * so applying an inbound change does not push it back.
 */
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
