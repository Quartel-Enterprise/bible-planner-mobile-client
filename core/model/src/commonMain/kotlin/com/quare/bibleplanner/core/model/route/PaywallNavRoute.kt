package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class PaywallNavRoute(
    val source: PaywallEntrySource,
) : NavRoute
