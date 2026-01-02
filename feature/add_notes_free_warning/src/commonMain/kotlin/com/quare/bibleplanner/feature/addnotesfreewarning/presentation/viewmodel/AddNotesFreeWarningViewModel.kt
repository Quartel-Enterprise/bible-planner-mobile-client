package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiAction
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class AddNotesFreeWarningViewModel : ViewModel() {
    private val _uiAction: MutableSharedFlow<AddNotesFreeWarningUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<AddNotesFreeWarningUiAction> = _uiAction

    fun onEvent(event: AddNotesFreeWarningUiEvent) {
        when (event) {
            AddNotesFreeWarningUiEvent.OnSubscribeToPremium -> {
                viewModelScope.launch {
                    _uiAction.emit(AddNotesFreeWarningUiAction.NavigateToPremium)
                }
            }

            AddNotesFreeWarningUiEvent.OnCancel -> {
                viewModelScope.launch {
                    _uiAction.emit(AddNotesFreeWarningUiAction.NavigateBack)
                }
            }
        }
    }
}
