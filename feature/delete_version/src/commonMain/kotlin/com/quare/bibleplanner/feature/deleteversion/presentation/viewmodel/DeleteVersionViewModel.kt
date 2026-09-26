package com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DeleteVersionViewModel(
    private val bibleVersionDownloaderFacade: BibleVersionDownloaderFacade,
    private val navigator: Navigator,
    route: DeleteVersionNavRoute,
    trackEvent: TrackEvent,
) : TrackedViewModel<DeleteVersionUiEvent>(trackEvent) {
    val uiState: StateFlow<DeleteVersionUiState>
        field = MutableStateFlow<DeleteVersionUiState>(DeleteVersionUiState.Idle)

    private val versionId = route.versionId

    override fun handleEvent(event: DeleteVersionUiEvent) {
        when (event) {
            DeleteVersionUiEvent.OnConfirmDelete -> {
                viewModelScope.launch {
                    uiState.update { DeleteVersionUiState.Loading }
                    bibleVersionDownloaderFacade.deleteDownload(versionId)
                    trackEvent(
                        name = AnalyticsEventNames.BIBLE_VERSION_DELETED,
                        params = mapOf(AnalyticsParams.VERSION_ID to versionId),
                    )
                    dismiss()
                }
            }

            DeleteVersionUiEvent.OnCancel -> dismiss()
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            navigator.navigateBack()
        }
    }
}
