package com.quare.bibleplanner.core.provider.analytics.domain.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

fun ReadingPlanType.toAnalyticsValue(): String = name.toPlanTypeAnalyticsValue()

fun String.toPlanTypeAnalyticsValue(): String = lowercase()
