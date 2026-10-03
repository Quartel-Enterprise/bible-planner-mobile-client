package com.quare.bibleplanner.core.provider.ads.domain.service

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdAvailability
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdResult
import kotlinx.coroutines.flow.StateFlow

interface RewardedAdService {
    val isSupported: Boolean
    val availability: StateFlow<RewardedAdAvailability>

    fun preload()

    suspend fun show(): RewardedAdResult
}
