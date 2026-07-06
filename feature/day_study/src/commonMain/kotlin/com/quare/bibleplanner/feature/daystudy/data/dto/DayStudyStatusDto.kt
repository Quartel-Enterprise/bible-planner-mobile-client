package com.quare.bibleplanner.feature.daystudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DayStudyStatusDto(
    @SerialName("is_unlocked") val isUnlocked: Boolean,
    @SerialName("used_count") val usedCount: Int,
    @SerialName("free_limit") val freeLimit: Int,
    @SerialName("is_pro") val isPro: Boolean,
    @SerialName("client_cache_token") val clientCacheToken: String,
)
