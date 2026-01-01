package com.quare.bibleplanner.feature.unlockpremium.presentation.factory

import bibleplanner.feature.unlock_premium.generated.resources.Res
import bibleplanner.feature.unlock_premium.generated.resources.month
import bibleplanner.feature.unlock_premium.generated.resources.plan_annual
import bibleplanner.feature.unlock_premium.generated.resources.plan_monthly
import bibleplanner.feature.unlock_premium.generated.resources.year
import com.quare.bibleplanner.feature.unlockpremium.domain.model.plan.SubscriptionPlanType
import com.quare.bibleplanner.feature.unlockpremium.domain.model.usecase.GetSubscriptionPlansUseCase
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.SubscriptionPlanPresentationModel
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiState

class UnlockPremiumUiStateFactory(
    private val getSubscriptionPlans: GetSubscriptionPlansUseCase,
) {
    fun create(): UnlockPremiumUiState = UnlockPremiumUiState(
        subscriptionPlans = getSubscriptionPlans().map { planModel ->
            when (planModel.planType) {
                SubscriptionPlanType.Monthly -> SubscriptionPlanPresentationModel(
                    title = Res.string.plan_monthly,
                    period = Res.string.month,
                    savePercentage = null,
                    isSelected = false,
                    price = 1.0,
                    type = planModel.planType,
                )

                SubscriptionPlanType.Annual -> SubscriptionPlanPresentationModel(
                    title = Res.string.plan_annual,
                    period = Res.string.year,
                    savePercentage = 20,
                    isSelected = true,
                    price = 10.0,
                    type = planModel.planType,
                )
            }
        },
    )
}
