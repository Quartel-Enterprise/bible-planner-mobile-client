package com.quare.bibleplanner.feature.paywall.presentation.utils

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.month
import bibleplanner.feature.paywall.generated.resources.per_month
import bibleplanner.feature.paywall.generated.resources.per_year
import bibleplanner.feature.paywall.generated.resources.plan_annual
import bibleplanner.feature.paywall.generated.resources.plan_annual_description
import bibleplanner.feature.paywall.generated.resources.plan_monthly
import bibleplanner.feature.paywall.generated.resources.plan_monthly_description
import bibleplanner.feature.paywall.generated.resources.year
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import org.jetbrains.compose.resources.StringResource

internal fun SubscriptionPlanType.toTitleResource(): StringResource = when (this) {
    SubscriptionPlanType.Monthly -> Res.string.plan_monthly
    SubscriptionPlanType.Annual -> Res.string.plan_annual
}

internal fun SubscriptionPlanType.toDescriptionResource(): StringResource = when (this) {
    SubscriptionPlanType.Monthly -> Res.string.plan_monthly_description
    SubscriptionPlanType.Annual -> Res.string.plan_annual_description
}

internal fun SubscriptionPlanType.toPeriodResource(): StringResource = when (this) {
    SubscriptionPlanType.Monthly -> Res.string.per_month
    SubscriptionPlanType.Annual -> Res.string.per_year
}

internal fun SubscriptionPlanType.toPeriodUnitResource(): StringResource = when (this) {
    SubscriptionPlanType.Monthly -> Res.string.month
    SubscriptionPlanType.Annual -> Res.string.year
}
