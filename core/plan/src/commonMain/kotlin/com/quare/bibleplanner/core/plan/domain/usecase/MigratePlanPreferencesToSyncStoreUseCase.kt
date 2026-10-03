package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.sync.PlanPreferenceKeys
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao

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
