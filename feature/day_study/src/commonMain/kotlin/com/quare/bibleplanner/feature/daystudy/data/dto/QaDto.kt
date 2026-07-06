package com.quare.bibleplanner.feature.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class QaDto(
    @SerialName("question") val question: String,
    @SerialName("answer") val answer: String,
)
