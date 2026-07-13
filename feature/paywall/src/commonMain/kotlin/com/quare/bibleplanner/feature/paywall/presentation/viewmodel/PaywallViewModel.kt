package com.quare.bibleplanner.feature.paywall.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isApple
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.feature.paywall.presentation.factory.PaywallUiStateFactory
import com.quare.bibleplanner.feature.paywall.presentation.mapper.PaywallAnalyticsReasonMapper
import com.quare.bibleplanner.feature.paywall.presentation.mapper.PaywallExceptionMapper
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiAction
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PaywallViewModel(
    private val factory: PaywallUiStateFactory,
    private val getPurchaseResultUseCase: GetPurchaseResultUseCase,
    private val getRestorePurchaseResultUseCase: GetRestorePurchaseResultUseCase,
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    private val exceptionMapper: PaywallExceptionMapper,
    private val analyticsReasonMapper: PaywallAnalyticsReasonMapper,
    private val observeIsProUser: ObserveIsProUser,
    trackEvent: TrackEvent,
    val platform: Platform,
) : TrackedViewModel<PaywallUiEvent>(trackEvent) {
    private val _uiState: MutableStateFlow<PaywallUiState> = MutableStateFlow(PaywallUiState.Loading)
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<PaywallUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<PaywallUiAction> = _uiAction

    private val storeName: String = if (platform.isApple()) "App Store" else "Google Play Store"

    private val store: String = if (platform.isApple()) APP_STORE else PLAY_STORE

    private var storePackages: List<StorePackage> = emptyList()

    private var purchaseInitiated = false

    init {
        viewModelScope.launch {
            _uiState.update { PaywallUiState.Loading }
            val initializationResult = factory.create(storeName)
            storePackages = initializationResult.storePackages
            _uiState.update { initializationResult.uiState }
        }
        observeProStatus()
    }

    private fun observeProStatus() {
        viewModelScope.launch {
            observeIsProUser()
                .drop(1)
                .filter { it }
                .collect {
                    if (!purchaseInitiated) {
                        _uiAction.emit(PaywallUiAction.NavigateBack)
                    }
                }
        }
    }

    private suspend fun ensureLoggedIn(): Boolean {
        if (getAuthenticatedUserId() != null) return true
        _uiAction.emit(PaywallUiAction.NavigateToLoginWarning(LoginWarningReason.Purchase.key))
        return false
    }

    override fun handleEvent(event: PaywallUiEvent) {
        when (event) {
            PaywallUiEvent.OnBackClick -> {
                trackEvent(
                    name = AnalyticsEventNames.PAYWALL_DISMISSED,
                    params = selectedPlanParams(),
                )
                viewModelScope.launch {
                    _uiAction.emit(PaywallUiAction.NavigateBack)
                }
            }

            PaywallUiEvent.OnStartProJourneyClick -> {
                viewModelScope.launch {
                    if (!ensureLoggedIn()) return@launch
                    val currentState = _uiState.value

                    if (currentState is PaywallUiState.Success) {
                        if (currentState.isPurchasing) return@launch

                        val selectedPlan = currentState.subscriptionPlans.firstOrNull { it.isSelected } ?: return@launch

                        val packageToPurchase = storePackages.find {
                            val pkgType = it.type
                            val planType = selectedPlan.type

                            (pkgType.name == "MONTHLY" && planType.name == "Monthly") ||
                                (pkgType.name == "ANNUAL" && planType.name == "Annual")
                        }

                        if (packageToPurchase != null) {
                            purchaseInitiated = true
                            trackPurchaseEvent(
                                name = AnalyticsEventNames.PURCHASE_STARTED,
                                plan = selectedPlan.type,
                                storePackage = packageToPurchase,
                            )
                            _uiState.update { currentState.copy(isPurchasing = true) }

                            getPurchaseResultUseCase(packageToPurchase)
                                .onSuccess {
                                    trackPurchaseEvent(
                                        name = AnalyticsEventNames.PURCHASE_COMPLETED,
                                        plan = selectedPlan.type,
                                        storePackage = packageToPurchase,
                                    )
                                    _uiState.update { currentState.copy(isPurchasing = false) }
                                    _uiAction.emit(PaywallUiAction.NavigateTo(CongratsNavRoute))
                                }.onFailure { error ->
                                    trackEvent(
                                        name = AnalyticsEventNames.PURCHASE_FAILED,
                                        params = mapOf(
                                            AnalyticsParams.REASON to analyticsReasonMapper.map(error),
                                            AnalyticsParams.SUBSCRIPTION_PLAN to selectedPlan.type.toAnalyticsValue(),
                                            AnalyticsParams.STORE to store,
                                        ),
                                    )
                                    _uiState.update { currentState.copy(isPurchasing = false) }
                                    val messageRes = exceptionMapper.map(error)
                                    _uiAction.emit(PaywallUiAction.ShowSnackbar(messageRes))
                                }
                        }
                    }
                }
            }

            is PaywallUiEvent.OnPlanSelected -> {
                if (selectedPlanType() != event.planType) {
                    trackEvent(
                        name = AnalyticsEventNames.PAYWALL_PLAN_SELECTED,
                        params = mapOf(AnalyticsParams.SUBSCRIPTION_PLAN to event.planType.toAnalyticsValue()),
                    )
                }
                _uiState.update { currentState ->
                    when (currentState) {
                        is PaywallUiState.Success -> currentState.copy(
                            subscriptionPlans = currentState.subscriptionPlans.map { plan ->
                                plan.copy(isSelected = plan.type == event.planType)
                            },
                        )

                        else -> currentState
                    }
                }
            }

            PaywallUiEvent.OnRestorePurchases -> {
                viewModelScope.launch {
                    if (!ensureLoggedIn()) return@launch
                    purchaseInitiated = true
                    val currentState = _uiState.value
                    if (currentState is PaywallUiState.Success) {
                        _uiState.update { currentState.copy(isPurchasing = true) }
                    }

                    getRestorePurchaseResultUseCase()
                        .onSuccess {
                            trackEvent(
                                name = AnalyticsEventNames.RESTORE_COMPLETED,
                                params = mapOf(AnalyticsParams.STORE to store),
                            )
                            if (currentState is PaywallUiState.Success) {
                                _uiState.update { currentState.copy(isPurchasing = false) }
                            }
                            _uiAction.emit(PaywallUiAction.NavigateTo(CongratsNavRoute))
                        }.onFailure { error ->
                            trackEvent(
                                name = AnalyticsEventNames.RESTORE_FAILED,
                                params = mapOf(
                                    AnalyticsParams.REASON to analyticsReasonMapper.map(error),
                                    AnalyticsParams.STORE to store,
                                ),
                            )
                            if (currentState is PaywallUiState.Success) {
                                _uiState.update { currentState.copy(isPurchasing = false) }
                            }
                            val messageRes = exceptionMapper.map(error)
                            _uiAction.emit(
                                PaywallUiAction.ShowSnackbar(
                                    message = messageRes,
                                    args = listOf(storeName),
                                ),
                            )
                        }
                }
            }
        }
    }

    private fun trackPurchaseEvent(
        name: String,
        plan: SubscriptionPlanType,
        storePackage: StorePackage,
    ) {
        trackEvent(
            name = name,
            params = mapOf(
                AnalyticsParams.SUBSCRIPTION_PLAN to plan.toAnalyticsValue(),
                AnalyticsParams.PACKAGE_ID to storePackage.identifier,
                AnalyticsParams.PRICE to storePackage.priceString,
                AnalyticsParams.STORE to store,
            ),
        )
    }

    private fun selectedPlanParams(): Map<String, Any> = selectedPlanType()
        ?.let { mapOf(AnalyticsParams.SUBSCRIPTION_PLAN to it.toAnalyticsValue()) }
        .orEmpty()

    private fun selectedPlanType(): SubscriptionPlanType? = (_uiState.value as? PaywallUiState.Success)
        ?.subscriptionPlans
        ?.firstOrNull { it.isSelected }
        ?.type

    private fun SubscriptionPlanType.toAnalyticsValue(): String = name.lowercase()

    private companion object {
        const val APP_STORE = "app_store"
        const val PLAY_STORE = "play_store"
    }
}
