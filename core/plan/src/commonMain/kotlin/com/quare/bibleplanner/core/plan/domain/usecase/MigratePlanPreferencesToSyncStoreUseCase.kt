package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.sync.PlanPreferenceKeys
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao

/*
 * Why: runs at startup regardless of auth so logged-out users keep their data; values
 * are written pending so they reach the backend after login. Idempotent via a
 * persisted flag, and legacy keys are dropped afterwards.
 */
class MigratePlanPreferencesToSyncStoreUseCase(
    private val planLocalDataSource: PlanLocalDataSource,
    private val syncedPreferenceDao: SyncedPreferenceDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke() {
        if (planLocalDataSource.hasMigratedPlanPreferences()) return
        val now = currentTimestampProvider.getCurrentTimestamp()
        planLocalDataSource.getLegacyPlanStartTimestamp()?.let { startEpoch ->
            syncedPreferenceDao.setLocal(
                key = PlanPreferenceKeys.PLAN_START_DATE,
                value = startEpoch.toString(),
                updatedAt = now,
            )
        }
        planLocalDataSource.getLegacySelectedReadingPlan()?.let { selectedPlan ->
            syncedPreferenceDao.setLocal(
                key = PlanPreferenceKeys.SELECTED_READING_PLAN,
                value = selectedPlan,
                updatedAt = now,
            )
        }
        planLocalDataSource.finishPlanPreferencesMigration()
    }
}
