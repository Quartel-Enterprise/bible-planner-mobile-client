package com.quare.bibleplanner.feature.materialyou.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.utils.orFalse
import com.quare.bibleplanner.feature.materialyou.domain.model.MaterialYouUseCases
import com.quare.bibleplanner.feature.materialyou.presentation.model.AndroidColorSchemeUiAction
import com.quare.bibleplanner.feature.materialyou.presentation.model.AndroidColorSchemeUiEvent
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AndroidColorSchemeViewModel(
    private val useCases: MaterialYouUseCases,
) : ViewModel() {
    private val _uiAction: Channel<AndroidColorSchemeUiAction> = Channel()
    val uiAction = _uiAction.receiveAsFlow()

    private val _uiState = MutableStateFlow(false)
    val uiState: StateFlow<Boolean> = _uiState.asStateFlow()

    init {
        observe(
            flow = useCases.getIsDynamicColorsEnabledFlow(),
            collector = {
                _uiState.value = it.orFalse()
            },
        )
    }

    fun onEvent(event: AndroidColorSchemeUiEvent) {
        viewModelScope.launch {
            when (event) {
                is AndroidColorSchemeUiEvent.OnIsDynamicColorsEnabledChange -> {
                    useCases.setIsDynamicColorsEnabled(event.isEnabled)
                }

                AndroidColorSchemeUiEvent.OnInformationDialogDismiss,
                AndroidColorSchemeUiEvent.BottomSheetGotItClick,
                -> {
                    _uiAction.send(AndroidColorSchemeUiAction.CloseBottomSheet)
                }
            }
        }
    }
}
