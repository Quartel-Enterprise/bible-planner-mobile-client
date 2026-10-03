package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.dto.EntitlementDto
import com.quare.bibleplanner.core.provider.billing.data.dto.SubscriberResponseDto
import com.quare.bibleplanner.core.provider.billing.domain.model.PRO_ENTITLEMENT_ID

internal class ProEntitlementMapper(
    private val epochMillisMapper: EpochMillisMapper,
) {
    fun mapActiveEntitlement(response: SubscriberResponseDto): EntitlementDto? = response.subscriber
        .entitlements[PRO_ENTITLEMENT_ID]
        ?.takeIf { entitlement ->
            entitlement.isActiveAt(response.requestDateMillis)
        }

    fun mapWillRenew(
        response: SubscriberResponseDto,
        productIdentifier: String,
    ): Boolean = response.subscriber
        .subscriptions[productIdentifier]
        ?.unsubscribeDetectedAt == null

    private fun EntitlementDto.isActiveAt(nowMillis: Long): Boolean {
        val expirationDates = listOfNotNull(expiresDate, gracePeriodExpiresDate)
        if (expirationDates.isEmpty()) return true
        val expirationMillis = expirationDates
            .mapNotNull(epochMillisMapper::map)
            .maxOrNull()
            ?: return false
        return expirationMillis > nowMillis
    }
}
