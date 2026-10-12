package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterAudioManifestSegmentDto(
    @SerialName("start_ms") val startMs: Long,
    // Why: the server sends each word as a bare [offset in the text, start ms from the segment, duration ms] array.
    @SerialName("words") val words: List<List<Long>>?,
)
