package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class AwaitProEntitlementUseCase(
    private val repository: DesktopBillingRepository,
) {
    private val pollInterval: Duration = 3.seconds
    private val pollTimeout: Duration = 10.minutes

    suspend operator fun invoke(): Boolean = withTimeoutOrNull(pollTimeout) {
        while (true) {
            delay(pollInterval)
            if (repository.refreshSubscriptionStatus() is SubscriptionStatus.Pro) break
        }
        true
    } == true
}
