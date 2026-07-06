package com.quare.bibleplanner.feature.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterRequestDto(
    @SerialName("number") val number: Int,
    @SerialName("start_verse") val startVerse: Int?,
    @SerialName("end_verse") val endVerse: Int?,
)
