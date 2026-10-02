package com.quare.bibleplanner.core.chapterstudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterStudyResponseDto(
    @SerialName("content") val content: ChapterStudyContentDto,
    @SerialName("model") val model: String,
    @SerialName("prompt_version") val promptVersion: Int,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_pro") val isPro: Boolean,
    @SerialName("client_cache_token") val clientCacheToken: String,
)
