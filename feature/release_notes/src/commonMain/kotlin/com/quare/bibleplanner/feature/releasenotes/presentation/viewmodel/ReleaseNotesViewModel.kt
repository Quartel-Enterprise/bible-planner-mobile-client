package com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.releasenotes.presentation.factory.ReleaseNotesUiStateFactory
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiAction
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiEvent
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReleaseNotesViewModel(
    private val uiStateFactory: ReleaseNotesUiStateFactory,
    private val trackEvent: TrackEvent,
    val platform: Platform,
) : ViewModel() {
    private val _uiState = MutableStateFlow(uiStateFactory.createInitialState())
    val uiState: StateFlow<ReleaseNotesUiState> = _uiState.asStateFlow()

    private val _uiAction = Channel<ReleaseNotesUiAction>()
    val uiAction = _uiAction.receiveAsFlow()

    init {
        loadReleaseNotes()
    }

    fun onEvent(event: ReleaseNotesUiEvent) {
        when (event) {
            is ReleaseNotesUiEvent.OnTabSelected -> {
                trackEvent(
                    name = AnalyticsEventNames.RELEASE_NOTES_TAB_SELECTED,
                    params = mapOf(AnalyticsParams.TAB to event.tab.name.lowercase()),
                )
                _uiState.update { state ->
                    if (state is ReleaseNotesUiState.Success) {
                        state.copy(currentTab = event.tab)
                    } else {
                        state
                    }
                }
            }

            ReleaseNotesUiEvent.OnBackClicked ->
                viewModelScope.launch { _uiAction.send(ReleaseNotesUiAction.NavigateBack) }

            ReleaseNotesUiEvent.OnGithubAllReleasesClicked -> {
                trackEvent(
                    name = AnalyticsEventNames.GITHUB_RELEASE_OPENED,
                    params = emptyMap(),
                )
                viewModelScope.launch {
                    _uiAction.send(
                        ReleaseNotesUiAction.OpenUrl(ALL_RELEASES_LINK),
                    )
                }
            }

            is ReleaseNotesUiEvent.OnGithubVersionClicked -> {
                trackEvent(
                    name = AnalyticsEventNames.GITHUB_RELEASE_OPENED,
                    params = mapOf(AnalyticsParams.VERSION to event.version),
                )
                viewModelScope.launch {
                    _uiAction.send(
                        ReleaseNotesUiAction.OpenUrl("$ALL_RELEASES_PREFIX${event.version}"),
                    )
                }
            }
        }
    }

    private fun loadReleaseNotes() {
        viewModelScope.launch {
            _uiState.update { ReleaseNotesUiState.Loading }
            val state = uiStateFactory.create()
            _uiState.update { state }
        }
    }

    companion object {
        private const val ALL_RELEASES_LINK =
            "https://github.com/Quartel-Enterprise/bible-planner-mobile-client/releases"
        private const val ALL_RELEASES_PREFIX = "$ALL_RELEASES_LINK/tag/"
    }
}
