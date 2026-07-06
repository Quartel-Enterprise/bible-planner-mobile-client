package com.quare.bibleplanner.feature.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DayStudyProgressDto(
    @SerialName("phase") val phase: String,
)
