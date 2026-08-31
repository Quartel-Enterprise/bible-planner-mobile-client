package com.quare.bibleplanner.feature.studysuggestion.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.preferences.study_suggestion.generated.resources.Res
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_disabled_message
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_enabled_message
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_mode_blocked_message
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.ObserveStudySuggestionSettings
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.SetStudySuggestionEnabled
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.SetStudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiAction
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

internal class StudySuggestionViewModel(
    observeStudySuggestionSettings: ObserveStudySuggestionSettings,
    private val setStudySuggestionEnabled: SetStudySuggestionEnabled,
    private val setStudySuggestionMode: SetStudySuggestionMode,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<StudySuggestionUiEvent>(trackEvent) {
    val uiState: StateFlow<Loadable<StudySuggestionSettingsModel>> = observeStudySuggestionSettings()
        .map<StudySuggestionSettingsModel, Loadable<StudySuggestionSettingsModel>> { Loadable.Loaded(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = Loadable.Loading,
        )

    private val _uiAction = MutableSharedFlow<StudySuggestionUiAction>()
    val uiAction: SharedFlow<StudySuggestionUiAction> = _uiAction

    override fun handleEvent(event: StudySuggestionUiEvent) {
        when (event) {
            is StudySuggestionUiEvent.OnToggleClick -> toggle(event.isNewValueOn)

            is StudySuggestionUiEvent.OnModeClick -> selectMode(event.mode)

            StudySuggestionUiEvent.OnBlockedModeClick -> showSnackbar(
                Res.string.study_suggestion_mode_blocked_message,
            )

            StudySuggestionUiEvent.OnDismiss -> navigator.navigateBack()
        }
    }

    private fun toggle(isNewValueOn: Boolean) {
        viewModelScope.launch {
            setStudySuggestionEnabled(isNewValueOn)
            val message = if (isNewValueOn) {
                Res.string.study_suggestion_enabled_message
            } else {
                Res.string.study_suggestion_disabled_message
            }
            _uiAction.emit(StudySuggestionUiAction.ShowSnackbar(message))
        }
    }

    private fun selectMode(mode: StudySuggestionMode) {
        viewModelScope.launch {
            setStudySuggestionMode(mode)
        }
    }

    private fun showSnackbar(message: StringResource) {
        viewModelScope.launch {
            _uiAction.emit(StudySuggestionUiAction.ShowSnackbar(message))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
