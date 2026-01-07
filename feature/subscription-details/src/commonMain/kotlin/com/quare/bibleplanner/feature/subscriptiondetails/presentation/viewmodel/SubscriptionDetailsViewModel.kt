package com.quare.bibleplanner.feature.subscriptiondetails.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.model.SubscriptionDetailsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SubscriptionDetailsViewModel(
    getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase?,
) : ViewModel() {
    val uiState: StateFlow<SubscriptionDetailsUiState> = (getSubscriptionStatusFlow?.invoke() ?: flowOf(null))
        .map { status ->
            if (status is SubscriptionStatus.Pro) {
                SubscriptionDetailsUiState.Loaded(
                    planName = status.planName,
                    purchaseDate = status.purchaseDate,
                    expirationDate = status.expirationDate,
                    willRenew = status.willRenew,
                )
            } else {
                SubscriptionDetailsUiState.Error
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SubscriptionDetailsUiState.Loading,
        )
}
