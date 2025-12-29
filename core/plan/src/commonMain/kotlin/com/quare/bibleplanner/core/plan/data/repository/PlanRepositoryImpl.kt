package com.quare.bibleplanner.core.plan.data.repository

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.date.toLocalDate
import com.quare.bibleplanner.core.date.toTimestampUTC
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.data.datasource.PlanLocalDataSource
import com.quare.bibleplanner.core.plan.data.mapper.WeekPlanDtoToModelMapper
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class PlanRepositoryImpl(
    private val planLocalDataSource: PlanLocalDataSource,
    private val weekPlanDtoToModelMapper: WeekPlanDtoToModelMapper,
    private val localDateTimeProvider: LocalDateTimeProvider,
) : PlanRepository {
    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> = planLocalDataSource
        .getPlans(readingPlanType)
        .map {
            weekPlanDtoToModelMapper.map(
                weekPlanDto = it,
            )
        }

    override suspend fun setStartPlanTimestamp(timestamp: Long) {
        planLocalDataSource.setPlanStartTimestamp(timestamp)
    }

    override suspend fun deleteStartPlanTimestamp() {
        planLocalDataSource.removeLocalDate()
    }

    override fun getStartPlanTimestamp(): Flow<LocalDate?> =
        planLocalDataSource.getPlanStartTimeStamp().map { timestamp ->
            timestamp?.let(localDateTimeProvider::getLocalDateTime)?.toLocalDate()
        }
}
