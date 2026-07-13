package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isAndroid
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.StartUpdate
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiAction
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class InAppUpdateViewModel(
    route: InAppUpdateNavRoute,
    platform: Platform,
    private val startUpdate: StartUpdate,
    trackEvent: TrackEvent,
) : TrackedViewModel<InAppUpdateUiEvent>(trackEvent) {
    private val source = route.source

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
        trackEvent(AnalyticsEventNames.UPDATE_PROMPT_SHOWN, promptParams(route.versionName))
    }

    override fun handleEvent(event: InAppUpdateUiEvent) {
        when (event) {
            InAppUpdateUiEvent.OnUpdateClick -> onUpdateClick()
            InAppUpdateUiEvent.OnDismiss -> onDismiss()
        }
    }

    private fun onUpdateClick() {
        trackEvent(AnalyticsEventNames.UPDATE_ACCEPTED, sourceParams())
        viewModelScope.launch {
            startUpdate()
            emitAction(InAppUpdateUiAction.NavigateBack)
        }
    }

    private fun onDismiss() {
        trackEvent(AnalyticsEventNames.UPDATE_DISMISSED, sourceParams())
        emitAction(InAppUpdateUiAction.NavigateBack)
    }

    private fun promptParams(versionName: String?): Map<String, Any> = buildMap {
        put(AnalyticsParams.SOURCE, source)
        versionName?.let { put(AnalyticsParams.VERSION, it) }
    }

    private fun sourceParams(): Map<String, Any> = mapOf(AnalyticsParams.SOURCE to source)

    private fun emitAction(action: InAppUpdateUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }
}
