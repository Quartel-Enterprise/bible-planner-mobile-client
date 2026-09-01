package com.quare.bibleplanner.feature.inappupdate.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CompleteUpdateInstall
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.launch

internal class UpdateDownloadedViewModel(
    private val completeUpdateInstall: CompleteUpdateInstall,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<UpdateDownloadedUiEvent>(trackEvent) {
    override fun handleEvent(event: UpdateDownloadedUiEvent) {
        when (event) {
            UpdateDownloadedUiEvent.OnRestartNowClick -> onRestartNowClick()
            UpdateDownloadedUiEvent.OnLaterClick -> navigator.navigateBack()
        }
    }

    private fun onRestartNowClick() {
        viewModelScope.launch { completeUpdateInstall() }
    }
}
