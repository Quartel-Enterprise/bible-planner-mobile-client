package com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.GetAppInstanceId

class GetAppInstanceIdUseCase(
    private val analyticsService: AnalyticsService,
) : GetAppInstanceId {
    override suspend fun invoke(): String? = analyticsService.getAppInstanceId()
}
