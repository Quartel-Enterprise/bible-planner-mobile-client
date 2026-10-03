package com.quare.bibleplanner.core.provider.ads.testing

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdAvailability
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdResult
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRewardedAdService(
    override val isSupported: Boolean,
    var showResult: RewardedAdResult,
    availability: RewardedAdAvailability,
) : RewardedAdService {
    val availabilityFlow = MutableStateFlow(availability)
    var preloadCount = 0
    var showCount = 0

    override val availability: StateFlow<RewardedAdAvailability> = availabilityFlow

    override fun preload() {
        preloadCount++
    }

    override suspend fun show(): RewardedAdResult {
        showCount++
        return showResult
    }
}
