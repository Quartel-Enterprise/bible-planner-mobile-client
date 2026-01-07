package com.quare.bibleplanner.core.provider.billing.mapper

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo

internal fun CustomerInfo.toProEntitlement(): EntitlementInfo? = entitlements.active["Bible Planner Pro"]
