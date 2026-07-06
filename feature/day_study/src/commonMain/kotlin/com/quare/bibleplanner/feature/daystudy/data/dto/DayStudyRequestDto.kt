package com.quare.bibleplanner.feature.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DayStudyRequestDto(
    @SerialName("passages") val passages: List<PassageRequestDto>,
    @SerialName("version") val version: String,
    @SerialName("language") val language: String,
)
