package com.quare.bibleplanner.feature.deleteprogress.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.ResetAllProgressUseCase
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiAction
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiEvent
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DeleteAllProgressViewModel(
    private val resetAllProgressUseCase: ResetAllProgressUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<DeleteAllProgressUiState> =
        MutableStateFlow(DeleteAllProgressUiState.Idle)
    val uiState: StateFlow<DeleteAllProgressUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<DeleteAllProgressUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<DeleteAllProgressUiAction> = _uiAction

    fun onEvent(event: DeleteAllProgressUiEvent) {
        when (event) {
            DeleteAllProgressUiEvent.OnConfirmDelete -> {
                viewModelScope.launch {
                    _uiState.update { DeleteAllProgressUiState.Loading }
                    try {
                        resetAllProgressUseCase()
                        _uiState.update { DeleteAllProgressUiState.Success }
                        _uiAction.emit(DeleteAllProgressUiAction.NavigateBack)
                    } catch (e: Exception) {
                        // On error, go back to idle state
                        _uiState.update { DeleteAllProgressUiState.Idle }
                    }
                }
            }

            DeleteAllProgressUiEvent.OnCancel -> {
                viewModelScope.launch {
                    _uiAction.emit(DeleteAllProgressUiAction.NavigateBack)
                }
            }
        }
    }
}
