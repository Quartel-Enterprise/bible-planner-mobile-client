package com.quare.bibleplanner.core.provider.billing.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SubscriptionDto(
    @SerialName("unsubscribe_detected_at")
    val unsubscribeDetectedAt: String?,
    @SerialName("billing_issues_detected_at")
    val billingIssuesDetectedAt: String?,
    @SerialName("store")
    val store: String?,
)
