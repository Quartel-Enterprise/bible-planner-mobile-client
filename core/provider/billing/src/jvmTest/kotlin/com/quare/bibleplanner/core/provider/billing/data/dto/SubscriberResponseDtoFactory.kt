package com.quare.bibleplanner.core.provider.billing.data.dto

import com.quare.bibleplanner.core.provider.billing.domain.model.PRO_ENTITLEMENT_ID

internal const val TEST_APP_USER_ID = "user-1"
internal const val TEST_REQUEST_DATE_MILLIS = 1_784_934_454_914L
internal const val TEST_MONTHLY_PRODUCT_ID = "prod_web_1"

internal fun proSubscriberResponse(
    expiresDate: String? = "2099-01-06T22:23:11Z",
    gracePeriodExpiresDate: String? = null,
    unsubscribeDetectedAt: String? = null,
    productIdentifier: String = TEST_MONTHLY_PRODUCT_ID,
    store: String? = "stripe",
): SubscriberResponseDto = SubscriberResponseDto(
    requestDateMillis = TEST_REQUEST_DATE_MILLIS,
    subscriber = SubscriberDto(
        entitlements = mapOf(
            PRO_ENTITLEMENT_ID to EntitlementDto(
                productIdentifier = productIdentifier,
                purchaseDate = "2026-01-06T21:23:11Z",
                expiresDate = expiresDate,
                gracePeriodExpiresDate = gracePeriodExpiresDate,
            ),
        ),
        subscriptions = mapOf(
            productIdentifier to SubscriptionDto(
                unsubscribeDetectedAt = unsubscribeDetectedAt,
                billingIssuesDetectedAt = null,
                store = store,
            ),
        ),
        originalAppUserId = TEST_APP_USER_ID,
    ),
)

internal fun freeSubscriberResponse(): SubscriberResponseDto = SubscriberResponseDto(
    requestDateMillis = TEST_REQUEST_DATE_MILLIS,
    subscriber = SubscriberDto(
        entitlements = emptyMap(),
        subscriptions = emptyMap(),
        originalAppUserId = TEST_APP_USER_ID,
    ),
)
