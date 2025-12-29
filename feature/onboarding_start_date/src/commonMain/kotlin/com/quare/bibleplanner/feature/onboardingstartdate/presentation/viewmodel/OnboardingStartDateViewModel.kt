package com.quare.bibleplanner.feature.onboardingstartdate.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.feature.onboardingstartdate.domain.repository.OnboardingStartDateRepository
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.model.OnboardingStartDateUiAction
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.model.OnboardingStartDateUiEvent
import com.quare.bibleplanner.feature.onboardingstartdate.presentation.model.OnboardingStartDateUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class OnboardingStartDateViewModel(
    private val planRepository: PlanRepository,
    private val onboardingRepository: OnboardingStartDateRepository,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : ViewModel() {
    private val _uiState: MutableStateFlow<OnboardingStartDateUiState> =
        MutableStateFlow(OnboardingStartDateUiState(isDontShowAgainMarked = false))
    val uiState: StateFlow<OnboardingStartDateUiState> = _uiState.asStateFlow()
    private val _uiAction: MutableSharedFlow<OnboardingStartDateUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<OnboardingStartDateUiAction> = _uiAction

    fun onEvent(event: OnboardingStartDateUiEvent) {
        when (event) {
            is OnboardingStartDateUiEvent.OnDismiss -> {
                viewModelScope.launch {
                    onboardingRepository.setDontShowAgain(_uiState.value.isDontShowAgainMarked)
                    _uiAction.emit(OnboardingStartDateUiAction.DISMISS)
                }
            }

            is OnboardingStartDateUiEvent.OnSetDateClick -> {
                onSetDateClick()
            }

            is OnboardingStartDateUiEvent.OnStartNowClick -> {
                onStartNowClick()
            }

            is OnboardingStartDateUiEvent.OnDontShowAgainClick -> {
                _uiState.update { it.copy(isDontShowAgainMarked = !it.isDontShowAgainMarked) }
            }
        }
    }

    private fun onStartNowClick() {
        viewModelScope.launch {
            val now = currentTimestampProvider.getCurrentTimestamp()
            planRepository.setStartPlanTimestamp(now)
            _uiAction.emit(OnboardingStartDateUiAction.DISMISS)
        }
    }

    private fun onSetDateClick() {
        viewModelScope.launch {
            _uiAction.emit(OnboardingStartDateUiAction.NAVIGATE_TO_SET_DATE)
        }
    }
}

