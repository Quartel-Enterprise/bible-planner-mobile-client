package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterAudioManifestSegmentDto(
    @SerialName("start_ms") val startMs: Long,
)
