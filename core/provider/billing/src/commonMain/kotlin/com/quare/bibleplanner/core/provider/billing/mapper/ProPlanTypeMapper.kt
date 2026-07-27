package com.quare.bibleplanner.core.provider.billing.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType

internal class ProPlanTypeMapper {
    fun map(productIdentifier: String): ProPlanType = when (productIdentifier) {
        APPLE_MONTHLY_PRODUCT_ID,
        PLAY_MONTHLY_PRODUCT_ID,
        WEB_MONTHLY_PRODUCT_ID,
        LEGACY_WEB_MONTHLY_PRODUCT_ID,
        -> ProPlanType.MONTHLY

        APPLE_ANNUAL_PRODUCT_ID,
        PLAY_ANNUAL_PRODUCT_ID,
        WEB_ANNUAL_PRODUCT_ID,
        LEGACY_WEB_ANNUAL_PRODUCT_ID,
        -> ProPlanType.ANNUAL

        else -> ProPlanType.UNKNOWN
    }

    private companion object {
        const val APPLE_MONTHLY_PRODUCT_ID = "com.quare.bibleplanner.premium.monthly"
        const val APPLE_ANNUAL_PRODUCT_ID = "com.quare.bibleplanner.premium.annual"
        const val PLAY_MONTHLY_PRODUCT_ID = "bibleplanner.premium:bibleplanner-premium-month"
        const val PLAY_ANNUAL_PRODUCT_ID = "bibleplanner.premium:bibleplanner-premium-year"
        const val WEB_MONTHLY_PRODUCT_ID = "prod_web_2"
        const val WEB_ANNUAL_PRODUCT_ID = "prod_web_2_yearly"
        const val LEGACY_WEB_MONTHLY_PRODUCT_ID = "prod_web_1"
        const val LEGACY_WEB_ANNUAL_PRODUCT_ID = "prod_web_1_yearly"
    }
}
