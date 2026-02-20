package com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DeleteVersionViewModel(
    savedStateHandle: SavedStateHandle,
    private val bibleVersionDownloaderFacade: BibleVersionDownloaderFacade,
) : ViewModel() {
    private val _uiState: MutableStateFlow<DeleteVersionUiState> =
        MutableStateFlow(DeleteVersionUiState.Idle)
    val uiState: StateFlow<DeleteVersionUiState> = _uiState.asStateFlow()

    private val versionId = savedStateHandle.toRoute<DeleteVersionNavRoute>().versionId

    private val _backUiAction: MutableSharedFlow<Unit> = MutableSharedFlow()
    val backUiAction: SharedFlow<Unit> = _backUiAction

    fun onEvent(event: DeleteVersionUiEvent) {
        when (event) {
            DeleteVersionUiEvent.OnConfirmDelete -> {
                viewModelScope.launch {
                    _uiState.update { DeleteVersionUiState.Loading }
                    bibleVersionDownloaderFacade.deleteDownload(versionId)
                    _uiState.update { DeleteVersionUiState.Idle }
                    dismiss()
                }
            }

            DeleteVersionUiEvent.OnCancel -> {
                dismiss()
            }
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            _backUiAction.emit(Unit)
        }
    }
}
