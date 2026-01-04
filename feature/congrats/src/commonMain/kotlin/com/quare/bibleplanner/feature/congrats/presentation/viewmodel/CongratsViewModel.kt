package com.quare.bibleplanner.feature.congrats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiAction
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CongratsViewModel : ViewModel() {
    private val _uiAction = Channel<CongratsUiAction>()
    val uiAction: Flow<CongratsUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: CongratsUiEvent) {
        when (event) {
            CongratsUiEvent.ON_DISMISS, CongratsUiEvent.ON_START_EXPLORING -> {
                viewModelScope.launch {
                    _uiAction.send(CongratsUiAction.Close)
                }
            }
        }
    }
}
