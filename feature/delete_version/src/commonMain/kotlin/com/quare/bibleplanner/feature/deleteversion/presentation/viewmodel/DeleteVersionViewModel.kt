package com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DeleteVersionViewModel(
    route: DeleteVersionNavRoute,
    private val bibleVersionDownloaderFacade: BibleVersionDownloaderFacade,
    trackEvent: TrackEvent,
) : TrackedViewModel<DeleteVersionUiEvent>(trackEvent) {
    private val _uiState: MutableStateFlow<DeleteVersionUiState> =
        MutableStateFlow(DeleteVersionUiState.Idle)
    val uiState: StateFlow<DeleteVersionUiState> = _uiState.asStateFlow()

    private val versionId = route.versionId

    private val _backUiAction: MutableSharedFlow<Unit> = MutableSharedFlow()
    val backUiAction: SharedFlow<Unit> = _backUiAction

    override fun handleEvent(event: DeleteVersionUiEvent) {
        when (event) {
            DeleteVersionUiEvent.OnConfirmDelete -> {
                viewModelScope.launch {
                    _uiState.update { DeleteVersionUiState.Loading }
                    bibleVersionDownloaderFacade.deleteDownload(versionId)
                    trackEvent(
                        name = AnalyticsEventNames.BIBLE_VERSION_DELETED,
                        params = mapOf(AnalyticsParams.VERSION_ID to versionId),
                    )
                    _uiState.update { DeleteVersionUiState.Idle }
                    dismiss()
                }
            }

            DeleteVersionUiEvent.OnCancel -> dismiss()
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            _backUiAction.emit(Unit)
        }
    }
}
