package com.quare.bibleplanner.feature.studysuggestion.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionEnabled
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.SetStudySuggestionSyncEnabled
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.studysuggestion.presentation.factory.StudySuggestionUiStateFactory
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionMessage
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiAction
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiEvent
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class StudySuggestionViewModel(
    private val setStudySuggestionEnabled: SetStudySuggestionEnabled,
    private val setStudySuggestionMode: SetStudySuggestionMode,
    private val setStudySuggestionSyncEnabled: SetStudySuggestionSyncEnabled,
    private val navigator: Navigator,
    uiStateFactory: StudySuggestionUiStateFactory,
    trackEvent: TrackEvent,
) : TrackedViewModel<StudySuggestionUiEvent>(trackEvent) {
    val uiState: StateFlow<StudySuggestionUiState> = uiStateFactory
        .create()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = uiStateFactory.createInitialState(),
        )

    val uiAction: SharedFlow<StudySuggestionUiAction>
        field = MutableSharedFlow<StudySuggestionUiAction>()

    override fun handleEvent(event: StudySuggestionUiEvent) {
        when (event) {
            is StudySuggestionUiEvent.OnToggleClick -> toggle(event.isNewValueOn)

            is StudySuggestionUiEvent.OnModeClick -> selectMode(event.mode)

            StudySuggestionUiEvent.OnBlockedModeClick -> showSnackbar(
                StudySuggestionMessage.MODE_BLOCKED,
            )

            is StudySuggestionUiEvent.SyncToggleClicked -> toggleSync(event.isNewValueOn)

            StudySuggestionUiEvent.SyncToggleBlockedClicked -> navigator.navigate(
                LoginWarningNavRoute(LoginWarningReason.Preferences.StudySuggestion.key),
            )

            StudySuggestionUiEvent.OnDismiss -> navigator.navigateBack()
        }
    }

    private fun toggle(isNewValueOn: Boolean) {
        viewModelScope.launch {
            setStudySuggestionEnabled(isNewValueOn)
            val message = if (isNewValueOn) {
                StudySuggestionMessage.ENABLED
            } else {
                StudySuggestionMessage.DISABLED
            }
            uiAction.emit(StudySuggestionUiAction.ShowSnackbar(message))
        }
    }

    private fun selectMode(mode: StudySuggestionMode) {
        viewModelScope.launch {
            setStudySuggestionMode(mode)
        }
    }

    private fun toggleSync(isNewValueOn: Boolean) {
        viewModelScope.launch {
            setStudySuggestionSyncEnabled(isNewValueOn)
        }
    }

    private fun showSnackbar(message: StudySuggestionMessage) {
        viewModelScope.launch {
            uiAction.emit(StudySuggestionUiAction.ShowSnackbar(message))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
