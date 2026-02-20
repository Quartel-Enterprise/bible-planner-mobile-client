package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetBibleVersionsByLanguageUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.SetSelectedVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiAction
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BibleVersionViewModel(
    getBibleVersionsUseCase: GetBibleVersionsByLanguageUseCase,
    private val setSelectedVersion: SetSelectedVersionUseCase,
    private val downloaderFacade: BibleVersionDownloaderFacade,
) : ViewModel() {
    private val _uiAction: MutableSharedFlow<BibleVersionUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<BibleVersionUiAction> = _uiAction

    val uiState: StateFlow<Map<Language, List<BibleModel>>> = getBibleVersionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    fun onEvent(event: BibleVersionUiEvent) {
        when (event) {
            is BibleVersionUiEvent.OnDownload -> downloadVersion(event.id)
            is BibleVersionUiEvent.OnPause -> pauseDownload(event.id)
            is BibleVersionUiEvent.OnResume -> resumeDownload(event.id)
            is BibleVersionUiEvent.OnDelete -> deleteVersion(event.id)
            is BibleVersionUiEvent.OnSelect -> selectVersion(event.id)
            BibleVersionUiEvent.OnDismiss -> dismiss()
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
        viewModelScope.launch {
            downloaderFacade.deleteDownload(id)
        }
    }

    private fun selectVersion(id: String) {
        viewModelScope.launch {
            setSelectedVersion(id)
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            _uiAction.emit(BibleVersionUiAction.BackToPreviousRoute)
        }
    }
}
