package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.sync.PlanPreferenceKeys
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao

/**
 * One-time migration of the reading-plan preferences from the legacy DataStore into the synced
 * key-value store. Runs at app startup (regardless of auth) so logged-out existing users keep their
 * data once reads move to the synced store. Existing values are written as pending so they reach the
 * backend once the user is authenticated (mirroring how legacy favorites are preserved). Idempotent:
 * gated by a persisted flag and the legacy keys are dropped afterwards.
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
