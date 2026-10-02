package com.quare.bibleplanner.core.chapterstudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterStudyProgressDto(
    @SerialName("phase") val phase: String,
)
