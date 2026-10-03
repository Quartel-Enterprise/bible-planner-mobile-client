package com.quare.bibleplanner.core.provider.analytics.domain.model

sealed interface EventAnalytics {
    sealed interface Track : EventAnalytics {
        data class Automatic(
            val name: String,
            val params: Map<String, Any>,
        ) : Track

        data class Manual(
            val names: Set<String>,
        ) : Track {
            constructor(name: String) : this(setOf(name))
        }
    }

    data object NotTracked : EventAnalytics
}
