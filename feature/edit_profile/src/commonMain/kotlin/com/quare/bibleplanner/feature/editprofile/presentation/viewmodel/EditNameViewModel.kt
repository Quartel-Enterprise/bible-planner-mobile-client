package com.quare.bibleplanner.feature.editprofile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_name_updated
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.editprofile.domain.usecase.UpdateDisplayName
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class EditNameViewModel(
    private val observeUserProfile: ObserveUserProfile,
    private val updateDisplayName: UpdateDisplayName,
    trackEvent: TrackEvent,
) : TrackedViewModel<EditNameUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<EditNameUiAction>()
    val uiAction: SharedFlow<EditNameUiAction> = _uiAction

    val uiState: StateFlow<EditNameUiState> = observeUserProfile()
        .map { profile ->
            EditNameUiState(currentName = Loadable.Loaded(profile?.displayName))
        }.onStart { emit(EditNameUiState(currentName = Loadable.Loading)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = EditNameUiState(currentName = Loadable.Loading),
        )

    override fun handleEvent(event: EditNameUiEvent) {
        when (event) {
            is EditNameUiEvent.OnSaveClick -> save(event.name)
        }
    }

    private fun save(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            navigateBack()
            return
        }
        viewModelScope.launch {
            if (trimmed == observeUserProfile().first()?.displayName) {
                _uiAction.emit(EditNameUiAction.NavigateBack)
                return@launch
            }
            updateDisplayName(trimmed)
            _uiAction.emit(EditNameUiAction.NavigateBack)
            _uiAction.emit(EditNameUiAction.ShowSnackbar(Res.string.edit_profile_name_updated))
        }
    }

    private fun navigateBack() {
        viewModelScope.launch { _uiAction.emit(EditNameUiAction.NavigateBack) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
