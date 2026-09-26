package com.quare.bibleplanner.core.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterSummaryDto(
    @SerialName("title") val title: String,
    @SerialName("body") val body: String,
)
