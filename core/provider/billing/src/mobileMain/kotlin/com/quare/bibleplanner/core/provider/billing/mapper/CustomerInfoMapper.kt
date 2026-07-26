package com.quare.bibleplanner.core.provider.billing.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.PRO_ENTITLEMENT_ID
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo

internal fun CustomerInfo.toProEntitlement(): EntitlementInfo? = entitlements.active[PRO_ENTITLEMENT_ID]
