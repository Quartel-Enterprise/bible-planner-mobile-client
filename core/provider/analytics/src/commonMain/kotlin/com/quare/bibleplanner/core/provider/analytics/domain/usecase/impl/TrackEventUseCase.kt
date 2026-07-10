package com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

class TrackEventUseCase(
    private val analyticsService: AnalyticsService,
) : TrackEvent {
    override fun invoke(
        name: String,
        params: Map<String, Any>,
    ) {
        analyticsService.logEvent(
            name = name,
            params = params.mapValues { (_, value) -> normalize(value) },
        )
    }

    private fun normalize(value: Any): Any = when (value) {
        is Boolean -> value.toString()
        is Int -> value.toLong()
        is Float -> value.toDouble()
        else -> value
    }
}
