package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateDownloadState
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.ObserveUpdateDownloadState
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateDownloadUiAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class InAppUpdateDownloadViewModel(
    observeUpdateDownloadState: ObserveUpdateDownloadState,
    private val trackEvent: TrackEvent,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<InAppUpdateDownloadUiAction>()
    val uiAction: SharedFlow<InAppUpdateDownloadUiAction> = _uiAction

    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress: StateFlow<Int?> = _downloadProgress

    init {
        observeUpdateDownloadState()
            .onEach(::onDownloadState)
            .launchIn(viewModelScope)
    }

    private suspend fun onDownloadState(state: UpdateDownloadState) {
        when (state) {
            is UpdateDownloadState.Downloading -> _downloadProgress.value = state.progress

            UpdateDownloadState.Downloaded -> {
                _downloadProgress.value = null
                _uiAction.emit(InAppUpdateDownloadUiAction.NavigateToDownloaded)
            }

            UpdateDownloadState.Failed -> {
                _downloadProgress.value = null
                trackEvent(AnalyticsEventNames.UPDATE_DOWNLOAD_FAILED, emptyMap())
                _uiAction.emit(InAppUpdateDownloadUiAction.ShowDownloadFailed)
            }

            UpdateDownloadState.Idle -> _downloadProgress.value = null
        }
    }
}
