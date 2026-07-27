package com.quare.bibleplanner.feature.subscriptiondetails.presentation.mapper

import bibleplanner.feature.subscription_details.generated.resources.Res
import bibleplanner.feature.subscription_details.generated.resources.plan_value_pro
import bibleplanner.feature.subscription_details.generated.resources.plan_value_pro_annual
import bibleplanner.feature.subscription_details.generated.resources.plan_value_pro_monthly
import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import org.jetbrains.compose.resources.StringResource

internal fun ProPlanType.toStringResource(): StringResource = when (this) {
    ProPlanType.MONTHLY -> Res.string.plan_value_pro_monthly
    ProPlanType.ANNUAL -> Res.string.plan_value_pro_annual
    ProPlanType.UNKNOWN -> Res.string.plan_value_pro
}
