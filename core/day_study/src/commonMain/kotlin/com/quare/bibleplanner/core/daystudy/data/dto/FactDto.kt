package com.quare.bibleplanner.core.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FactDto(
    @SerialName("label") val label: String,
    @SerialName("value") val value: String,
)
