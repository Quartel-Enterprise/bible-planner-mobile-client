package com.quare.bibleplanner.core.provider.ads.data.service

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdFailureReason
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdDataSource

internal class UnsupportedRewardedAdDataSource : RewardedAdDataSource {
    override val isSupported: Boolean = false

    override fun load(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (RewardedAdFailureReason) -> Unit,
    ) {
        onFailed(RewardedAdFailureReason.LOAD_ERROR)
    }

    override fun show(
        onEarned: () -> Unit,
        onDismissed: () -> Unit,
        onFailed: () -> Unit,
    ) {
        onFailed()
    }
}
