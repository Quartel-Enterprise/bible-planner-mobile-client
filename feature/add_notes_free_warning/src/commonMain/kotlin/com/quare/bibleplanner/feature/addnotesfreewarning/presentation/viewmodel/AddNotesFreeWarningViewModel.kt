package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiAction
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class AddNotesFreeWarningViewModel(
    route: AddNotesFreeWarningNavRoute,
) : ViewModel() {
    val maxFreeNotesAmount = route.maxFreeNotesAmount

    private val _uiAction: MutableSharedFlow<AddNotesFreeWarningUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<AddNotesFreeWarningUiAction> = _uiAction

    fun onEvent(event: AddNotesFreeWarningUiEvent) {
        viewModelScope.launch {
            _uiAction.emit(event.toUiAction())
        }
    }

    private fun AddNotesFreeWarningUiEvent.toUiAction() = when (this) {
        AddNotesFreeWarningUiEvent.OnCancel -> AddNotesFreeWarningUiAction.NavigateBack
        AddNotesFreeWarningUiEvent.OnSubscribeToPro -> AddNotesFreeWarningUiAction.NavigateToPro
    }
}
