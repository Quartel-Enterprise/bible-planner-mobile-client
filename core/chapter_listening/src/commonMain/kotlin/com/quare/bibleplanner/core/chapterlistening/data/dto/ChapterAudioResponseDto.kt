package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterAudioResponseDto(
    @SerialName("audio_url") val audioUrl: String,
    @SerialName("manifest_url") val manifestUrl: String,
)
