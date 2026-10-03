package com.quare.bibleplanner.core.provider.ads.data.service

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdAvailability
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdFailureReason
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdResult
import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentService
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdDataSource
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdService
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdUnitIdProvider
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal class RewardedAdServiceImpl(
    private val dataSource: RewardedAdDataSource,
    private val consentService: AdsConsentService,
    private val adUnitIdProvider: RewardedAdUnitIdProvider,
    private val applicationScope: ApplicationScope,
) : RewardedAdService {
    override val isSupported: Boolean
        get() = dataSource.isSupported && adUnitIdProvider().isNotBlank()

    override val availability: StateFlow<RewardedAdAvailability>
        field = MutableStateFlow(RewardedAdAvailability.IDLE)

    private val inFlightOrReady = setOf(RewardedAdAvailability.LOADING, RewardedAdAvailability.READY)

    override fun preload() {
        if (!isSupported || availability.value in inFlightOrReady) return
        availability.value = RewardedAdAvailability.LOADING
        applicationScope.launch {
            consentService.gatherConsent()
            availability.value = if (consentService.canRequestAds()) load() else RewardedAdAvailability.LOAD_ERROR
        }
    }

    override suspend fun show(): RewardedAdResult {
        val current = availability.value
        if (current != RewardedAdAvailability.READY) {
            return RewardedAdResult.Failed(current.toFailureReason())
        }
        availability.value = RewardedAdAvailability.IDLE
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                var hasEarned = false
                dataSource.show(
                    onEarned = { hasEarned = true },
                    onDismissed = {
                        val result = if (hasEarned) RewardedAdResult.Earned else RewardedAdResult.Dismissed
                        if (continuation.isActive) continuation.resume(result)
                    },
                    onFailed = {
                        if (continuation.isActive) {
                            continuation.resume(RewardedAdResult.Failed(RewardedAdFailureReason.SHOW_ERROR))
                        }
                    },
                )
            }
        }
    }

    private suspend fun load(): RewardedAdAvailability = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            dataSource.load(
                adUnitId = adUnitIdProvider(),
                onLoaded = { if (continuation.isActive) continuation.resume(RewardedAdAvailability.READY) },
                onFailed = { reason -> if (continuation.isActive) continuation.resume(reason.toAvailability()) },
            )
        }
    }

    private fun RewardedAdFailureReason.toAvailability(): RewardedAdAvailability = when (this) {
        RewardedAdFailureReason.NO_FILL -> RewardedAdAvailability.NO_FILL
        RewardedAdFailureReason.LOAD_ERROR, RewardedAdFailureReason.SHOW_ERROR -> RewardedAdAvailability.LOAD_ERROR
    }

    private fun RewardedAdAvailability.toFailureReason(): RewardedAdFailureReason = when (this) {
        RewardedAdAvailability.NO_FILL -> RewardedAdFailureReason.NO_FILL

        RewardedAdAvailability.IDLE,
        RewardedAdAvailability.LOADING,
        RewardedAdAvailability.READY,
        RewardedAdAvailability.LOAD_ERROR,
        -> RewardedAdFailureReason.LOAD_ERROR
    }
}
