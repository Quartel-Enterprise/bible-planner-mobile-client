package com.quare.bibleplanner.core.plan.data.repository

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.date.toLocalDate
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.mapper.ReadingPlanPreferenceMapper
import com.quare.bibleplanner.core.plan.data.mapper.WeekPlanDtoToModelMapper
import com.quare.bibleplanner.core.plan.data.sync.PlanPreferenceKeys
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.provider.room.dao.SyncedPreferenceDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

// Why: user writes are flagged pending so the sync engine pushes them; the provisional default
// (seedDefaultStartDate) is non-pending so it never overwrites a real remote value.
class PlanRepositoryImpl(
    private val planLocalDataSource: PlanLocalDataSource,
    private val weekPlanDtoToModelMapper: WeekPlanDtoToModelMapper,
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val readingPlanPreferenceMapper: ReadingPlanPreferenceMapper,
    private val syncedPreferenceDao: SyncedPreferenceDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : PlanRepository {
    // Why: bundled plans never change at runtime and parsing costs 52 JSON files per plan, so they
    // are cached for the process; the lock makes parallel cold callers share one parse.
    private val plansByType = mutableMapOf<ReadingPlanType, List<WeekPlanModel>>()
    private val plansMutex = Mutex()

    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> = plansMutex.withLock {
        plansByType.getOrPut(readingPlanType) {
            planLocalDataSource
                .getPlans(readingPlanType)
                .map {
                    weekPlanDtoToModelMapper.map(
                        weekPlanDto = it,
                    )
                }
        }
    }

    override suspend fun setStartPlanTimestamp(timestamp: Long) {
        syncedPreferenceDao.setLocal(
            key = PlanPreferenceKeys.PLAN_START_DATE,
            value = timestamp.toString(),
            updatedAt = currentTimestampProvider.getCurrentTimestamp(),
        )
    }

    override fun getStartPlanTimestamp(): Flow<LocalDate?> =
        syncedPreferenceDao.observeValue(PlanPreferenceKeys.PLAN_START_DATE).map { value ->
            value?.toLongOrNull()?.let(localDateTimeProvider::getLocalDateTime)?.toLocalDate()
        }

    override fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType> =
        syncedPreferenceDao.observeValue(PlanPreferenceKeys.SELECTED_READING_PLAN).map(
            readingPlanPreferenceMapper::mapPreferenceToModel,
        )

    override suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType) {
        syncedPreferenceDao.setLocal(
            key = PlanPreferenceKeys.SELECTED_READING_PLAN,
            value = readingPlanPreferenceMapper.mapModelToPreference(readingPlanType),
            updatedAt = currentTimestampProvider.getCurrentTimestamp(),
        )
    }

    override suspend fun seedDefaultStartDate(timestamp: Long) {
        syncedPreferenceDao.seedProvisional(
            key = PlanPreferenceKeys.PLAN_START_DATE,
            value = timestamp.toString(),
        )
    }
}
