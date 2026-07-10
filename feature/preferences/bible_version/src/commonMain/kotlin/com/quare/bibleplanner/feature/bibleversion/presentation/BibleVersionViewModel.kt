package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBibleVersionsUseCase
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.SetSelectedVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.factory.BibleVersionsUiStateFactory
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiAction
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionsUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BibleVersionViewModel(
    private val setSelectedVersion: SetSelectedVersionUseCase,
    private val downloaderFacade: BibleVersionDownloaderFacade,
    private val initializeBibleVersions: InitializeBibleVersionsUseCase,
    private val trackEvent: TrackEvent,
    uiStateFactory: BibleVersionsUiStateFactory,
) : ViewModel() {
    private val _uiAction: MutableSharedFlow<BibleVersionUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<BibleVersionUiAction> = _uiAction

    val uiState: StateFlow<BibleVersionsUiState> = uiStateFactory
        .create()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BibleVersionsUiState.Loading,
        )

    fun onEvent(event: BibleVersionUiEvent) {
        when (event) {
            is BibleVersionUiEvent.OnDownload -> downloadVersion(event.id)

            is BibleVersionUiEvent.OnPause -> pauseDownload(event.id)

            is BibleVersionUiEvent.OnResume -> resumeDownload(event.id)

            is BibleVersionUiEvent.OnDelete -> deleteVersion(event.id)

            is BibleVersionUiEvent.OnSelect -> selectVersion(event.id)

            BibleVersionUiEvent.OnDismiss -> dismiss()

            BibleVersionUiEvent.TryToDownloadBibleVersionsAgain -> {
                viewModelScope.launch {
                    initializeBibleVersions()
                }
            }
        }
    }

    private fun downloadVersion(id: String) {
        trackDownloadStarted(
            id = id,
            isResume = false,
        )
        downloaderFacade.downloadVersion(id)
        if (downloaderFacade.shouldShowDownloadTip) {
            emitUiAction(BibleVersionUiAction.ShowDownloadTip)
        }
    }

    private fun pauseDownload(id: String) {
        trackEvent(
            name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_PAUSED,
            params = mapOf(AnalyticsParams.VERSION_ID to id),
        )
        viewModelScope.launch {
            downloaderFacade.pauseDownload(id)
        }
    }

    private fun resumeDownload(id: String) {
        trackDownloadStarted(
            id = id,
            isResume = true,
        )
        viewModelScope.launch {
            downloaderFacade.downloadVersion(id)
        }
    }

    private fun deleteVersion(id: String) {
        emitUiAction(BibleVersionUiAction.NavigateToRoute(DeleteVersionNavRoute(id)))
    }

    private fun selectVersion(id: String) {
        if (!isAlreadySelected(id)) {
            trackEvent(
                name = AnalyticsEventNames.BIBLE_VERSION_SELECTED,
                params = mapOf(AnalyticsParams.VERSION_ID to id),
            )
        }
        viewModelScope.launch {
            setSelectedVersion(id)
        }
    }

    private fun isAlreadySelected(id: String): Boolean = (uiState.value as? BibleVersionsUiState.Success)
        ?.data
        ?.values
        ?.flatten()
        ?.any { it.version.id == id && it.isSelected } == true

    private fun trackDownloadStarted(
        id: String,
        isResume: Boolean,
    ) {
        trackEvent(
            name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_STARTED,
            params = mapOf(
                AnalyticsParams.VERSION_ID to id,
                AnalyticsParams.IS_RESUME to isResume,
            ),
        )
    }

    private fun dismiss() {
        emitUiAction(BibleVersionUiAction.BackToPreviousRoute)
    }

    private fun emitUiAction(uiAction: BibleVersionUiAction) {
        viewModelScope.launch {
            _uiAction.emit(uiAction)
        }
    }
}
