package com.quare.bibleplanner.feature.paywall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isApple
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.feature.paywall.presentation.factory.PaywallUiStateFactory
import com.quare.bibleplanner.feature.paywall.presentation.mapper.PaywallExceptionMapper
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiAction
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
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
    private val observeIsProUser: ObserveIsProUser,
    val platform: Platform,
) : ViewModel() {
    private val _uiState: MutableStateFlow<PaywallUiState> = MutableStateFlow(PaywallUiState.Loading)
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<PaywallUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<PaywallUiAction> = _uiAction

    private val storeName: String = if (platform.isApple()) "App Store" else "Google Play Store"

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

    fun onEvent(event: PaywallUiEvent) {
        when (event) {
            PaywallUiEvent.OnBackClick -> {
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
                            _uiState.update { currentState.copy(isPurchasing = true) }

                            getPurchaseResultUseCase(packageToPurchase)
                                .onSuccess {
                                    _uiState.update { currentState.copy(isPurchasing = false) }
                                    _uiAction.emit(PaywallUiAction.NavigateTo(CongratsNavRoute))
                                }.onFailure { error ->
                                    _uiState.update { currentState.copy(isPurchasing = false) }
                                    val messageRes = exceptionMapper.map(error)
                                    _uiAction.emit(PaywallUiAction.ShowSnackbar(messageRes))
                                }
                        }
                    }
                }
            }

            is PaywallUiEvent.OnPlanSelected -> {
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
                            if (currentState is PaywallUiState.Success) {
                                _uiState.update { currentState.copy(isPurchasing = false) }
                            }
                            _uiAction.emit(PaywallUiAction.NavigateTo(CongratsNavRoute))
                        }.onFailure { error ->
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
}
