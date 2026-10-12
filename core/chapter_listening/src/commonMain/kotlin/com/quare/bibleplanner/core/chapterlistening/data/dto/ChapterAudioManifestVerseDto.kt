package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
internal data class ChapterAudioManifestVerseDto(
    @SerialName("number") val number: Int,
    @SerialName("start_ms") val startMs: Long,
    @SerialName("duration_ms") val durationMs: Long,
    @SerialName("heading") val heading: ChapterAudioManifestSegmentDto?,
    // Why: read loosely, so one malformed word costs only its own highlight instead of the whole manifest.
    @SerialName("words") val words: JsonArray?,
)
