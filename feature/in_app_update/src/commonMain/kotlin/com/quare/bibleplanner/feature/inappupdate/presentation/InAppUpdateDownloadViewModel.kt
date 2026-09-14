package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.UpdateDownloadedNavRoute
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
    private val navigator: Navigator,
    private val trackEvent: TrackEvent,
) : ViewModel() {
    val uiAction: SharedFlow<InAppUpdateDownloadUiAction>
        field = MutableSharedFlow<InAppUpdateDownloadUiAction>()

    val downloadProgress: StateFlow<Int?>
        field = MutableStateFlow<Int?>(null)

    init {
        observeUpdateDownloadState()
            .onEach(::onDownloadState)
            .launchIn(viewModelScope)
    }

    private suspend fun onDownloadState(state: UpdateDownloadState) {
        when (state) {
            is UpdateDownloadState.Downloading -> downloadProgress.value = state.progress

            UpdateDownloadState.Downloaded -> {
                downloadProgress.value = null
                navigator.navigate(UpdateDownloadedNavRoute)
            }

            UpdateDownloadState.Failed -> {
                downloadProgress.value = null
                trackEvent(AnalyticsEventNames.UPDATE_DOWNLOAD_FAILED, emptyMap())
                uiAction.emit(InAppUpdateDownloadUiAction.ShowDownloadFailed)
            }

            UpdateDownloadState.Idle -> downloadProgress.value = null
        }
    }
}
