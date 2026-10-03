package com.quare.bibleplanner.core.provider.ads.data.service

import android.content.Context
import co.touchlab.kermit.Logger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdFailureReason
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdDataSource
import com.quare.bibleplanner.core.provider.platform.CurrentActivityProvider
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import kotlinx.coroutines.launch

internal class AndroidRewardedAdDataSource(
    private val context: Context,
    private val currentActivityProvider: CurrentActivityProvider,
    private val applicationScope: ApplicationScope,
) : RewardedAdDataSource {
    override val isSupported: Boolean = true

    private var rewardedAd: RewardedAd? = null
    private var isInitialized = false
    private val logger = Logger.withTag(LOG_TAG)

    override fun load(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (RewardedAdFailureReason) -> Unit,
    ) {
        initializeOnce()
        RewardedAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    onLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    logger.w { "Rewarded ad failed to load: ${error.code} ${error.message}" }
                    onFailed(error.toFailureReason())
                }
            },
        )
    }

    override fun show(
        onEarned: () -> Unit,
        onDismissed: () -> Unit,
        onFailed: () -> Unit,
    ) {
        val ad = rewardedAd
        val activity = currentActivityProvider.activity
        if (ad == null || activity == null) {
            onFailed()
            return
        }
        rewardedAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                logger.w { "Rewarded ad failed to show: ${error.code} ${error.message}" }
                onFailed()
            }
        }
        ad.show(activity) { onEarned() }
    }

    private fun initializeOnce() {
        if (isInitialized) return
        isInitialized = true
        applicationScope.launch { MobileAds.initialize(context) }
    }

    private fun LoadAdError.toFailureReason(): RewardedAdFailureReason = when (code) {
        AdRequest.ERROR_CODE_NO_FILL, AdRequest.ERROR_CODE_MEDIATION_NO_FILL -> RewardedAdFailureReason.NO_FILL
        else -> RewardedAdFailureReason.LOAD_ERROR
    }

    private companion object {
        const val LOG_TAG = "RewardedAd"
    }
}
