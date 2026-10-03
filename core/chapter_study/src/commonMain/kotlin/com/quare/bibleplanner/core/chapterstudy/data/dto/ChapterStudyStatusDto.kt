package com.quare.bibleplanner.core.chapterstudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChapterStudyStatusDto(
    @SerialName("is_unlocked") val isUnlocked: Boolean,
    @SerialName("used_count") val usedCount: Int,
    @SerialName("free_limit") val freeLimit: Int,
    @SerialName("is_pro") val isPro: Boolean,
    @SerialName("client_cache_token") val clientCacheToken: String,
    @SerialName("rewarded_remaining_today") val rewardedRemainingToday: Int,
)
