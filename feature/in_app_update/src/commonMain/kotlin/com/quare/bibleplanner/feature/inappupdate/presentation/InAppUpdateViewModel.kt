package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isAndroid
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.StartUpdate
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiAction
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class InAppUpdateViewModel(
    route: InAppUpdateNavRoute,
    platform: Platform,
    private val startUpdate: StartUpdate,
    private val trackEvent: TrackEvent,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<InAppUpdateUiAction>()
    val uiAction: SharedFlow<InAppUpdateUiAction> = _uiAction

    private val _uiState = MutableStateFlow(
        InAppUpdateUiState(
            versionName = route.versionName,
            isAndroid = platform.isAndroid(),
        ),
    )
    val uiState: StateFlow<InAppUpdateUiState> = _uiState

    init {
        trackEvent(AnalyticsEventNames.UPDATE_PROMPT_SHOWN, emptyMap())
    }

    fun onEvent(event: InAppUpdateUiEvent) {
        when (event) {
            InAppUpdateUiEvent.OnUpdateClick -> onUpdateClick()
            InAppUpdateUiEvent.OnDismiss -> emitAction(InAppUpdateUiAction.NavigateBack)
        }
    }

    private fun onUpdateClick() {
        trackEvent(AnalyticsEventNames.UPDATE_ACCEPTED, emptyMap())
        viewModelScope.launch {
            startUpdate()
            emitAction(InAppUpdateUiAction.NavigateBack)
        }
    }

    private fun emitAction(action: InAppUpdateUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }
}
