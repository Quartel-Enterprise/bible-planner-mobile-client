package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class PaywallTeaserNavRoute(
    val reason: PaywallTeaserReason,
) : NavRoute
