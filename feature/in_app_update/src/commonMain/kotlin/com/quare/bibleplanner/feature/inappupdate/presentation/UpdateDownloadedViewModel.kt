package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CompleteUpdateInstall
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiAction
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class UpdateDownloadedViewModel(
    private val completeUpdateInstall: CompleteUpdateInstall,
    trackEvent: TrackEvent,
) : TrackedViewModel<UpdateDownloadedUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<UpdateDownloadedUiAction>()
    val uiAction: SharedFlow<UpdateDownloadedUiAction> = _uiAction

    override fun handleEvent(event: UpdateDownloadedUiEvent) {
        when (event) {
            UpdateDownloadedUiEvent.OnRestartNowClick -> onRestartNowClick()
            UpdateDownloadedUiEvent.OnLaterClick -> emitAction(UpdateDownloadedUiAction.NavigateBack)
        }
    }

    private fun onRestartNowClick() {
        viewModelScope.launch { completeUpdateInstall() }
    }

    private fun emitAction(action: UpdateDownloadedUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }
}
