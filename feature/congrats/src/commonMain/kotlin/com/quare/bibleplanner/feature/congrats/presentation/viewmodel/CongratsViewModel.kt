package com.quare.bibleplanner.feature.congrats.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiAction
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CongratsViewModel(
    trackEvent: TrackEvent,
) : TrackedViewModel<CongratsUiEvent>(trackEvent) {
    private val _uiAction = Channel<CongratsUiAction>()
    val uiAction: Flow<CongratsUiAction> = _uiAction.receiveAsFlow()

    override fun handleEvent(event: CongratsUiEvent) {
        when (event) {
            CongratsUiEvent.OnDismiss, CongratsUiEvent.OnStartExploring -> {
                viewModelScope.launch {
                    _uiAction.send(CongratsUiAction.Close)
                }
            }
        }
    }
}
