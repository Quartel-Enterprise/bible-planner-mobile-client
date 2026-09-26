package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.PlansModel
import kotlinx.coroutines.flow.Flow

internal fun interface ObservePlansByWeek {
    operator fun invoke(): Flow<PlansModel>
}
