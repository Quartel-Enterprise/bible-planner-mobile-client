package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlinx.coroutines.flow.Flow

fun interface GetSelectedReadingPlanFlow {
    operator fun invoke(): Flow<ReadingPlanType>
}
