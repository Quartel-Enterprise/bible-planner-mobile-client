package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
internal data class ChapterAudioManifestSegmentDto(
    @SerialName("start_ms") val startMs: Long,
    // Why: read loosely, so one malformed word costs only its own highlight instead of the whole manifest.
    @SerialName("words") val words: JsonArray?,
)
