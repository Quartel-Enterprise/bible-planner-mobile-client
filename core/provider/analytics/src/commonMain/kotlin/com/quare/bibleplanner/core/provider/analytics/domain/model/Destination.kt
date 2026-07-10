package com.quare.bibleplanner.core.provider.analytics.domain.model

data class Destination(
    val name: String,
    val type: DestinationType,
    val params: Map<String, Any>,
)
