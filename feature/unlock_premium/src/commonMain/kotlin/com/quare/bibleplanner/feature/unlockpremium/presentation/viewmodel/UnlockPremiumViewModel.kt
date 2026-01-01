package com.quare.bibleplanner.feature.unlockpremium.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.unlockpremium.domain.model.plan.SubscriptionPlanType
import com.quare.bibleplanner.feature.unlockpremium.presentation.factory.UnlockPremiumUiStateFactory
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiAction
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiEvent
import com.quare.bibleplanner.feature.unlockpremium.presentation.model.UnlockPremiumUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class UnlockPremiumViewModel(
    factory: UnlockPremiumUiStateFactory,
) : ViewModel() {
    private val _uiState: MutableStateFlow<UnlockPremiumUiState> = MutableStateFlow(factory.create())
    val uiState: StateFlow<UnlockPremiumUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<UnlockPremiumUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<UnlockPremiumUiAction> = _uiAction

    fun onEvent(event: UnlockPremiumUiEvent) {
        when (event) {
            UnlockPremiumUiEvent.OnBackClick -> {
                viewModelScope.launch {
                    _uiAction.emit(UnlockPremiumUiAction.NavigateBack)
                }
            }

            UnlockPremiumUiEvent.OnStartPremiumJourney -> {
                // Mock action - empty implementation
                viewModelScope.launch {
                    // Empty action
                }
            }

            is UnlockPremiumUiEvent.OnPlanSelected -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        subscriptionPlans = currentState.subscriptionPlans.map { plan ->
                            plan.copy(isSelected = plan.type == event.planType)
                        },
                    )
                }
            }
        }
    }
}
