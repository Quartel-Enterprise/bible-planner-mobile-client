package com.quare.bibleplanner.feature.paywall.presentation.factory

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.month
import bibleplanner.feature.paywall.generated.resources.per_month
import bibleplanner.feature.paywall.generated.resources.per_year
import bibleplanner.feature.paywall.generated.resources.plan_annual
import bibleplanner.feature.paywall.generated.resources.plan_annual_description
import bibleplanner.feature.paywall.generated.resources.plan_monthly
import bibleplanner.feature.paywall.generated.resources.plan_monthly_description
import bibleplanner.feature.paywall.generated.resources.year
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ObserveIsChapterListeningEnabled
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultUseCase
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.feature.paywall.presentation.model.FixedPremiumFeature
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.feature.paywall.presentation.model.PremiumFeature
import com.quare.bibleplanner.feature.paywall.presentation.model.SubscriptionPlanPresentationModel
import com.quare.bibleplanner.feature.paywall.presentation.model.UnlimitedNotesPremiumFeature
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class PaywallUiStateFactory(
    private val getOfferingsResult: GetOfferingsResultUseCase,
    private val getMaxFreeNotesAmount: GetMaxFreeNotesAmountUseCase,
    private val observeIsChapterListeningEnabled: ObserveIsChapterListeningEnabled,
) {
    data class PaywallInitializationResult(
        val uiState: PaywallUiState,
        val storePackages: List<StorePackage>,
    )

    suspend fun create(storeName: String): PaywallInitializationResult {
        val maxFreeNotes = getMaxFreeNotesAmount()
        // Why: listening is only sold where it exists (mobile, switched on, a version with narration).
        val (offeringsResult, isChapterListeningAvailable) = coroutineScope {
            val isListeningAvailable = async {
                suspendRunCatching { observeIsChapterListeningEnabled().first() }.getOrDefault(false)
            }
            getOfferingsResult() to isListeningAvailable.await()
        }
        return offeringsResult.fold(
            onSuccess = { offerings ->
                val monthlyPackage = offerings.find { it.type == StorePackageType.MONTHLY }
                val subscriptionPlans = offerings.mapNotNull { storePackage ->
                    storePackage.toPresentationModel(getSavePercentage(storePackage, monthlyPackage))
                }

                if (subscriptionPlans.isEmpty()) {
                    Logger.e { "No subscription plans available for store: $storeName" }
                    PaywallInitializationResult(PaywallUiState.Error, storePackages = emptyList())
                } else {
                    val initialPlans = subscriptionPlans
                        .map { plan ->
                            if (plan.type == SubscriptionPlanType.Annual) {
                                plan.copy(isSelected = true)
                            } else {
                                plan
                            }
                        }.let { plans ->
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
                            isPurchasing = false,
                            storeName = storeName,
                            premiumFeatures = buildPremiumFeatures(
                                maxFreeNotes = maxFreeNotes,
                                isChapterListeningAvailable = isChapterListeningAvailable,
                            ),
                        ),
                        storePackages = offerings,
                    )
                }
            },
            onFailure = { throwable ->
                Logger.e(throwable) { "Failed to initialize paywall" }
                PaywallInitializationResult(PaywallUiState.Error, storePackages = emptyList())
            },
        )
    }

    private fun buildPremiumFeatures(
        maxFreeNotes: Int,
        isChapterListeningAvailable: Boolean,
    ): List<PremiumFeature> = buildList {
        add(FixedPremiumFeature.AI_DAY_STUDY)
        add(FixedPremiumFeature.AI_CHAPTER_STUDY)
        add(FixedPremiumFeature.AI_CHAT)
        if (isChapterListeningAvailable) {
            add(FixedPremiumFeature.CHAPTER_LISTENING)
        }
        add(FixedPremiumFeature.AD_FREE)
        add(UnlimitedNotesPremiumFeature(maxFreeNotes))
        add(FixedPremiumFeature.HIGHLIGHT_COLORS)
        add(FixedPremiumFeature.SHARE_IMAGE_STYLE)
    }

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

        val descriptionRes = when (planType) {
            SubscriptionPlanType.Monthly -> Res.string.plan_monthly_description
            SubscriptionPlanType.Annual -> Res.string.plan_annual_description
        }

        val periodRes = when (planType) {
            SubscriptionPlanType.Monthly -> Res.string.per_month
            SubscriptionPlanType.Annual -> Res.string.per_year
        }

        val periodUnitRes = when (planType) {
            SubscriptionPlanType.Monthly -> Res.string.month
            SubscriptionPlanType.Annual -> Res.string.year
        }

        return SubscriptionPlanPresentationModel(
            title = titleRes,
            description = descriptionRes,
            period = periodRes,
            periodUnit = periodUnitRes,
            savePercentage = savePercent,
            isSelected = false,
            priceDescription = priceString,
            type = planType,
        )
    }
}
