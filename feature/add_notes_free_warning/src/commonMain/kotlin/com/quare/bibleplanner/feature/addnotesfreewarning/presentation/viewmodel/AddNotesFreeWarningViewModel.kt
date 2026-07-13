package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiAction
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class AddNotesFreeWarningViewModel(
    route: AddNotesFreeWarningNavRoute,
    trackEvent: TrackEvent,
) : TrackedViewModel<AddNotesFreeWarningUiEvent>(trackEvent) {
    val maxFreeNotesAmount = route.maxFreeNotesAmount

    private val _uiAction: MutableSharedFlow<AddNotesFreeWarningUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<AddNotesFreeWarningUiAction> = _uiAction

    override fun handleEvent(event: AddNotesFreeWarningUiEvent) {
        viewModelScope.launch {
            _uiAction.emit(event.toUiAction())
        }
    }

    private fun AddNotesFreeWarningUiEvent.toUiAction() = when (this) {
        AddNotesFreeWarningUiEvent.OnCancel -> AddNotesFreeWarningUiAction.NavigateBack
        AddNotesFreeWarningUiEvent.OnSubscribeToPro -> AddNotesFreeWarningUiAction.NavigateToPro
    }
}
