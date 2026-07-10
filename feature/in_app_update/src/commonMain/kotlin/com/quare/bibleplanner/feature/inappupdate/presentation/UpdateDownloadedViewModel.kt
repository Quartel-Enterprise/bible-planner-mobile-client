package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CompleteUpdateInstall
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiAction
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class UpdateDownloadedViewModel(
    private val completeUpdateInstall: CompleteUpdateInstall,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<UpdateDownloadedUiAction>()
    val uiAction: SharedFlow<UpdateDownloadedUiAction> = _uiAction

    fun onEvent(event: UpdateDownloadedUiEvent) {
        when (event) {
            UpdateDownloadedUiEvent.OnRestartNowClick -> viewModelScope.launch { completeUpdateInstall() }
            UpdateDownloadedUiEvent.OnLaterClick -> emitAction(UpdateDownloadedUiAction.NavigateBack)
        }
    }

    private fun emitAction(action: UpdateDownloadedUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }
}
