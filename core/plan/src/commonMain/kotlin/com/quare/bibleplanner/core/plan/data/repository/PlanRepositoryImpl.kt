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
import kotlinx.datetime.LocalDate

/**
 * Reads/writes the reading-plan scalar preferences through the synced key-value store
 * ([SyncedPreferenceDao]); bundled JSON plans still come from [PlanLocalDataSource]. A user write
 * flags the value pending so the sync engine pushes it; a provisional default
 * ([seedDefaultStartDate]) is non-pending so it never overwrites a real remote value.
 */
class PlanRepositoryImpl(
    private val planLocalDataSource: PlanLocalDataSource,
    private val weekPlanDtoToModelMapper: WeekPlanDtoToModelMapper,
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val readingPlanPreferenceMapper: ReadingPlanPreferenceMapper,
    private val syncedPreferenceDao: SyncedPreferenceDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : PlanRepository {
    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> = planLocalDataSource
        .getPlans(readingPlanType)
        .map {
            weekPlanDtoToModelMapper.map(
                weekPlanDto = it,
            )
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
