package com.quare.bibleplanner.feature.paywall.presentation.factory

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.month
import bibleplanner.feature.paywall.generated.resources.plan_annual
import bibleplanner.feature.paywall.generated.resources.plan_monthly
import bibleplanner.feature.paywall.generated.resources.year
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultUseCase
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.feature.paywall.presentation.model.SubscriptionPlanPresentationModel

class PaywallUiStateFactory(
    private val getOfferingsResult: GetOfferingsResultUseCase,
) {
    data class PaywallInitializationResult(
        val uiState: PaywallUiState,
        val storePackages: List<StorePackage> = emptyList(),
    )

    suspend fun create(): PaywallInitializationResult = getOfferingsResult()
        .fold(
            onSuccess = { offerings ->
                val monthlyPackage = offerings.find { it.type == StorePackageType.MONTHLY }
                val subscriptionPlans = offerings.mapNotNull { storePackage ->
                    storePackage.toPresentationModel(getSavePercentage(storePackage, monthlyPackage))
                }

                if (subscriptionPlans.isEmpty()) {
                    PaywallInitializationResult(PaywallUiState.Error)
                } else {
                    // Select Annual by default if available, otherwise first
                    val initialPlans = subscriptionPlans
                        .map { plan ->
                            if (plan.type == SubscriptionPlanType.Annual) {
                                plan.copy(isSelected = true)
                            } else {
                                plan
                            }
                        }.let { plans ->
                            // If no annual plan, select the first one
                            if (plans.none { it.isSelected } && plans.isNotEmpty()) {
                                plans.mapIndexed { index, plan ->
                                    if (index == 0) plan.copy(isSelected = true) else plan
                                }
                            } else {
                                plans
                            }
                        }

                    PaywallInitializationResult(
                        uiState = PaywallUiState.Success(
                            subscriptionPlans = initialPlans,
                            isPurchasing = false
                        ),
                        storePackages = offerings,
                    )
                }
            },
            onFailure = {
                PaywallInitializationResult(PaywallUiState.Error)
            },
        )

    private fun getSavePercentage(
        storePackage: StorePackage,
        monthlyPackage: StorePackage?,
    ): Int? = if (storePackage.type == StorePackageType.ANNUAL && monthlyPackage != null) {
        val monthlyPrice = monthlyPackage.priceMicros.toDouble()
        val annualPrice = storePackage.priceMicros.toDouble()
        if (monthlyPrice > 0) {
            val annualMonthlyPrice = annualPrice / 12.0
            val savings = (monthlyPrice - annualMonthlyPrice) / monthlyPrice
            (savings * 100).toInt()
        } else {
            null
        }
    } else {
        null
    }

    private fun StorePackage.toPresentationModel(savePercent: Int? = null): SubscriptionPlanPresentationModel? {
        val planType = when (type) {
            StorePackageType.MONTHLY -> SubscriptionPlanType.Monthly
            StorePackageType.ANNUAL -> SubscriptionPlanType.Annual
            StorePackageType.UNKNOWN -> return null
        }

        val titleRes = when (planType) {
            SubscriptionPlanType.Monthly -> Res.string.plan_monthly
            SubscriptionPlanType.Annual -> Res.string.plan_annual
        }

        val periodRes = when (planType) {
            SubscriptionPlanType.Monthly -> Res.string.month
            SubscriptionPlanType.Annual -> Res.string.year
        }

        return SubscriptionPlanPresentationModel(
            title = titleRes,
            period = periodRes,
            savePercentage = savePercent,
            isSelected = false,
            priceDescription = priceString,
            type = planType,
        )
    }
}
