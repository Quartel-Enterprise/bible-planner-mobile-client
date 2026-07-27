package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.provider.billing.data.dto.SubscriberResponseDto
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.mapper.ProPlanTypeMapper
import kotlinx.datetime.LocalDateTime

internal class SubscriptionStatusMapper(
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val proPlanTypeMapper: ProPlanTypeMapper,
    private val proEntitlementMapper: ProEntitlementMapper,
    private val epochMillisMapper: EpochMillisMapper,
) {
    fun map(response: SubscriberResponseDto?): SubscriptionStatus {
        val entitlement = response
            ?.let(proEntitlementMapper::mapActiveEntitlement)
            ?: return SubscriptionStatus.Free
        return SubscriptionStatus.Pro(
            planType = proPlanTypeMapper.map(entitlement.productIdentifier),
            purchaseDate = entitlement.purchaseDate?.toLocalDateTime(),
            expirationDate = entitlement.expiresDate?.toLocalDateTime(),
            willRenew = proEntitlementMapper.mapWillRenew(
                response = response,
                productIdentifier = entitlement.productIdentifier,
            ),
        )
    }

    private fun String.toLocalDateTime(): LocalDateTime? = epochMillisMapper
        .map(this)
        ?.let(localDateTimeProvider::getLocalDateTime)
}
