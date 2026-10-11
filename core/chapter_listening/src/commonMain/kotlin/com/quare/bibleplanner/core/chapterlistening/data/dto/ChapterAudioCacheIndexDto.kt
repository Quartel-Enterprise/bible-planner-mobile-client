package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterAudioCacheIndexDto(
    @SerialName("entries") val entries: List<ChapterAudioCacheEntryDto>,
)
