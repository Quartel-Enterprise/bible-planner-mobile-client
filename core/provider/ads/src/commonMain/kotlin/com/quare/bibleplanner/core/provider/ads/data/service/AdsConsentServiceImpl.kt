package com.quare.bibleplanner.core.provider.ads.data.service

import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentDataSource
import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal class AdsConsentServiceImpl(
    private val dataSource: AdsConsentDataSource,
) : AdsConsentService {
    override val isPrivacyOptionsRequired: StateFlow<Boolean>
        field = MutableStateFlow(false)

    private val gatherMutex = Mutex()
    private var hasGathered = false

    override suspend fun gatherConsent() {
        gatherMutex.withLock {
            if (hasGathered) return
            hasGathered = withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { continuation ->
                    dataSource.gatherConsent { isInfoUpdated ->
                        if (continuation.isActive) continuation.resume(isInfoUpdated)
                    }
                }
            }
            refreshPrivacyOptionsRequirement()
        }
    }

    override fun canRequestAds(): Boolean = dataSource.canRequestAds()

    override suspend fun showPrivacyOptions() {
        awaitOnMain(dataSource::showPrivacyOptions)
        refreshPrivacyOptionsRequirement()
    }

    private fun refreshPrivacyOptionsRequirement() {
        isPrivacyOptionsRequired.value = dataSource.isPrivacyOptionsRequired()
    }

    private suspend fun awaitOnMain(request: (onComplete: () -> Unit) -> Unit) {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                request {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        }
    }
}
