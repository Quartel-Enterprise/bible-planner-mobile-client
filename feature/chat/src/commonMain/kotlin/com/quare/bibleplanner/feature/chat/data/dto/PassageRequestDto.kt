package com.quare.bibleplanner.feature.chat.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PassageRequestDto(
    @SerialName("book") val book: String,
    @SerialName("chapters") val chapters: List<ChapterRequestDto>,
)
