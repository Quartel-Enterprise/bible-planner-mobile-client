package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SubscriberDto(
    @SerialName("entitlements")
    val entitlements: Map<String, EntitlementDto>,
    @SerialName("subscriptions")
    val subscriptions: Map<String, SubscriptionDto>,
    @SerialName("original_app_user_id")
    val originalAppUserId: String,
)
