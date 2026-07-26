package com.quare.bibleplanner.core.provider.billing.mapper

internal class ProPlanNameMapper {
    fun map(productIdentifier: String): String = when (productIdentifier) {
        APPLE_MONTHLY_PRODUCT_ID, WEB_MONTHLY_PRODUCT_ID, LEGACY_WEB_MONTHLY_PRODUCT_ID -> MONTHLY_PLAN_NAME
        APPLE_ANNUAL_PRODUCT_ID, WEB_ANNUAL_PRODUCT_ID, LEGACY_WEB_ANNUAL_PRODUCT_ID -> ANNUAL_PLAN_NAME
        else -> productIdentifier
    }

    private companion object {
        const val APPLE_MONTHLY_PRODUCT_ID = "com.quare.bibleplanner.premium.monthly"
        const val APPLE_ANNUAL_PRODUCT_ID = "com.quare.bibleplanner.premium.annual"
        const val WEB_MONTHLY_PRODUCT_ID = "prod_web_2"
        const val WEB_ANNUAL_PRODUCT_ID = "prod_web_2_yearly"
        const val LEGACY_WEB_MONTHLY_PRODUCT_ID = "prod_web_1"
        const val LEGACY_WEB_ANNUAL_PRODUCT_ID = "prod_web_1_yearly"
        const val MONTHLY_PLAN_NAME = "Pro / Monthly Plan"
        const val ANNUAL_PLAN_NAME = "Pro / Annual Plan"
    }
}
