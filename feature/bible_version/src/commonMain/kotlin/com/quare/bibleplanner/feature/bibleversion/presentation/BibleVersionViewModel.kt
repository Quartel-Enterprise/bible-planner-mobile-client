package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBibleVersionsUseCase
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.SetSelectedVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.factory.BibleVersionsUiStateFactory
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiAction
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionsUiState
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BibleVersionViewModel(
    private val setSelectedVersion: SetSelectedVersionUseCase,
    private val downloaderFacade: BibleVersionDownloaderFacade,
    private val initializeBibleVersions: InitializeBibleVersionsUseCase,
    uiStateFactory: BibleVersionsUiStateFactory,
) : ViewModel() {
    private val _uiAction: MutableSharedFlow<BibleVersionUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<BibleVersionUiAction> = _uiAction

    private val _uiState: MutableStateFlow<BibleVersionsUiState> = MutableStateFlow(BibleVersionsUiState.Loading)

    val uiState: StateFlow<BibleVersionsUiState> = uiStateFactory
        .create()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BibleVersionsUiState.Loading,
        )

    init {
        observe(uiStateFactory.create()) { newState ->
            _uiState.update { newState }
        }
    }

    fun onEvent(event: BibleVersionUiEvent) {
        when (event) {
            is BibleVersionUiEvent.OnDownload -> {
                downloadVersion(event.id)
            }

            is BibleVersionUiEvent.OnPause -> {
                pauseDownload(event.id)
            }

            is BibleVersionUiEvent.OnResume -> {
                resumeDownload(event.id)
            }

            is BibleVersionUiEvent.OnDelete -> {
                deleteVersion(event.id)
            }

            is BibleVersionUiEvent.OnSelect -> {
                selectVersion(event.id)
            }

            BibleVersionUiEvent.OnDismiss -> {
                dismiss()
            }

            BibleVersionUiEvent.TryToDownloadBibleVersionsAgain -> {
                _uiState.update { BibleVersionsUiState.Loading }
                viewModelScope.launch {
                    initializeBibleVersions()
                }
            }
        }
    }

    private fun downloadVersion(id: String) {
        downloaderFacade.downloadVersion(id)
    }

    private fun pauseDownload(id: String) {
        viewModelScope.launch {
            downloaderFacade.pauseDownload(id)
        }
    }

    private fun resumeDownload(id: String) {
        viewModelScope.launch {
            downloaderFacade.downloadVersion(id)
        }
    }

    private fun deleteVersion(id: String) {
        emitUiAction(BibleVersionUiAction.NavigateToRoute(DeleteVersionNavRoute(id)))
    }

    private fun selectVersion(id: String) {
        viewModelScope.launch {
            setSelectedVersion(id)
        }
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
