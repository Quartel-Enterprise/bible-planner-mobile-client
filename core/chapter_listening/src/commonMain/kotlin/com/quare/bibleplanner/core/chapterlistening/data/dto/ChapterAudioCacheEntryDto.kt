package com.quare.bibleplanner.core.chapterlistening.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterAudioCacheEntryDto(
    @SerialName("chapter_key") val chapterKey: String,
    @SerialName("file_name") val fileName: String,
    @SerialName("bytes") val bytes: Long,
    @SerialName("last_used_at") val lastUsedAt: Long,
)
