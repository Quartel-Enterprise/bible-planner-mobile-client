package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.StartUpdate
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class InAppUpdateViewModel(
    route: InAppUpdateNavRoute,
    private val startUpdate: StartUpdate,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<InAppUpdateUiEvent>(trackEvent) {
    private val source = route.source

    private val _uiState = MutableStateFlow(InAppUpdateUiState(versionName = route.versionName))
    val uiState: StateFlow<InAppUpdateUiState> = _uiState

    init {
        trackEvent(AnalyticsEventNames.UPDATE_PROMPT_SHOWN, getPromptParams(route.versionName))
    }

    override fun handleEvent(event: InAppUpdateUiEvent) {
        when (event) {
            InAppUpdateUiEvent.OnUpdateClick -> onUpdateClick()
            InAppUpdateUiEvent.OnDismiss -> onDismiss()
        }
    }

    private fun onUpdateClick() {
        trackEvent(AnalyticsEventNames.UPDATE_ACCEPTED, getSourceParams())
        viewModelScope.launch {
            startUpdate()
            navigator.navigateBack()
        }
    }

    private fun onDismiss() {
        trackEvent(AnalyticsEventNames.UPDATE_DISMISSED, getSourceParams())
        navigator.navigateBack()
    }

    private fun getPromptParams(versionName: String?): Map<String, Any> = buildMap {
        put(AnalyticsParams.SOURCE, source)
        versionName?.let { put(AnalyticsParams.VERSION, it) }
    }

    private fun getSourceParams(): Map<String, Any> = mapOf(AnalyticsParams.SOURCE to source)
}
