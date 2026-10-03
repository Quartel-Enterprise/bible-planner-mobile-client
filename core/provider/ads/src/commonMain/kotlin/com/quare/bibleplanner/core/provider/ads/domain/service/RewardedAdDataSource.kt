package com.quare.bibleplanner.core.provider.ads.domain.service

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdFailureReason

interface RewardedAdDataSource {
    val isSupported: Boolean

    fun load(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (RewardedAdFailureReason) -> Unit,
    )

    fun show(
        onEarned: () -> Unit,
        onDismissed: () -> Unit,
        onFailed: () -> Unit,
    )
}
