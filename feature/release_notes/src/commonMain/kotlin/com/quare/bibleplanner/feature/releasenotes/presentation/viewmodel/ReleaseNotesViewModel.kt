package com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.releasenotes.presentation.factory.ReleaseNotesUiStateFactory
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiAction
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiEvent
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReleaseNotesViewModel internal constructor(
    private val uiStateFactory: ReleaseNotesUiStateFactory,
    private val navigator: Navigator,
    val platform: Platform,
    trackEvent: TrackEvent,
) : TrackedViewModel<ReleaseNotesUiEvent>(trackEvent) {
    val uiState: StateFlow<ReleaseNotesUiState>
        field = MutableStateFlow(uiStateFactory.createInitialState())

    private val _uiAction = Channel<ReleaseNotesUiAction>()
    val uiAction = _uiAction.receiveAsFlow()

    init {
        loadReleaseNotes()
    }

    override fun handleEvent(event: ReleaseNotesUiEvent) {
        when (event) {
            is ReleaseNotesUiEvent.OnTabSelected -> selectTab(event.tab)

            ReleaseNotesUiEvent.OnBackClicked -> navigator.navigateBack()

            ReleaseNotesUiEvent.OnGithubAllReleasesClicked -> {
                viewModelScope.launch {
                    _uiAction.send(
                        ReleaseNotesUiAction.OpenUrl(ALL_RELEASES_LINK),
                    )
                }
            }

            is ReleaseNotesUiEvent.OnGithubVersionClicked -> {
                viewModelScope.launch {
                    _uiAction.send(
                        ReleaseNotesUiAction.OpenUrl("$ALL_RELEASES_PREFIX${event.version}"),
                    )
                }
            }
        }
    }

    private fun selectTab(tab: ReleaseNotesTab) {
        val state = uiState.value as? ReleaseNotesUiState.Success ?: return
        if (state.currentTab == tab) return
        trackEvent(
            name = AnalyticsEventNames.RELEASE_NOTES_TAB_SELECTED,
            params = mapOf(AnalyticsParams.TAB to tab.name.lowercase()),
        )
        uiState.value = state.copy(currentTab = tab)
    }

    private fun loadReleaseNotes() {
        viewModelScope.launch {
            uiState.update { ReleaseNotesUiState.Loading }
            val state = uiStateFactory.create()
            uiState.update { state }
        }
    }

    companion object {
        private const val ALL_RELEASES_LINK =
            "https://github.com/Quartel-Enterprise/bible-planner-mobile-client/releases"
        private const val ALL_RELEASES_PREFIX = "$ALL_RELEASES_LINK/tag/"
    }
}
