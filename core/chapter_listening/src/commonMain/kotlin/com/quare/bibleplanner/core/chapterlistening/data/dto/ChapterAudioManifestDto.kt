package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterAudioManifestDto(
    @SerialName("duration_ms") val durationMs: Long,
    @SerialName("verses") val verses: List<ChapterAudioManifestVerseDto>,
)
