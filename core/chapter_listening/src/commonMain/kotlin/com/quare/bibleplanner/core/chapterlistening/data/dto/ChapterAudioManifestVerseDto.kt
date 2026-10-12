package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterAudioManifestVerseDto(
    @SerialName("number") val number: Int,
    @SerialName("start_ms") val startMs: Long,
    @SerialName("duration_ms") val durationMs: Long,
    @SerialName("heading") val heading: ChapterAudioManifestSegmentDto?,
)
