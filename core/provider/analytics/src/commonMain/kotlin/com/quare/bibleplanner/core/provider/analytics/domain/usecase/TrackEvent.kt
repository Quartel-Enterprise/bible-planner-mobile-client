package com.quare.bibleplanner.core.provider.analytics.domain.usecase

fun interface TrackEvent {
    operator fun invoke(
        name: String,
        params: Map<String, Any>,
    )
}
