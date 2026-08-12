package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChatContextRequestDto(
    @SerialName("type") val type: String,
    @SerialName("version") val version: String,
    @SerialName("passages") val passages: List<PassageRequestDto>,
    @SerialName("day_number") val dayNumber: Int?,
    @SerialName("week_number") val weekNumber: Int?,
    @SerialName("reading_plan_type") val readingPlanType: String?,
)
