package com.quare.bibleplanner.feature.congrats.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.launch

class CongratsViewModel(
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<CongratsUiEvent>(trackEvent) {
    override fun handleEvent(event: CongratsUiEvent) {
        when (event) {
            CongratsUiEvent.OnDismiss, CongratsUiEvent.OnStartExploring -> {
                viewModelScope.launch {
                    navigator.navigateBack()
                }
            }
        }
    }
}
