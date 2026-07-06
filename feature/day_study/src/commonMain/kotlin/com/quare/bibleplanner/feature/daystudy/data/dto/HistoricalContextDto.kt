package com.quare.bibleplanner.feature.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HistoricalContextDto(
    @SerialName("body") val body: String,
    @SerialName("facts") val facts: List<FactDto>,
)
