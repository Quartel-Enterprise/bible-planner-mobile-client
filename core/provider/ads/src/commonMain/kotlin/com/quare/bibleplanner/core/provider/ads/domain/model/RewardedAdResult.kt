package com.quare.bibleplanner.core.provider.ads.domain.model

sealed interface RewardedAdResult {
    data object Earned : RewardedAdResult

    data object Dismissed : RewardedAdResult

    data class Failed(
        val reason: RewardedAdFailureReason,
    ) : RewardedAdResult
}
