package com.quare.bibleplanner.feature.deleteprogress.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.ResetAllProgressUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.utils.suspendRunCatching
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
    private val resetAllProgress: ResetAllProgressUseCase,
    private val trackEvent: TrackEvent,
) : ViewModel() {
    private val _uiState: MutableStateFlow<DeleteAllProgressUiState> =
        MutableStateFlow(DeleteAllProgressUiState.Idle)
    val uiState: StateFlow<DeleteAllProgressUiState> = _uiState.asStateFlow()

    private val _backUiAction: MutableSharedFlow<Unit> = MutableSharedFlow()
    val backUiAction: SharedFlow<Unit> = _backUiAction

    fun onEvent(event: DeleteAllProgressUiEvent) {
        when (event) {
            DeleteAllProgressUiEvent.OnConfirmDelete -> {
                viewModelScope.launch {
                    _uiState.update { DeleteAllProgressUiState.Loading }
                    suspendRunCatching { resetAllProgress() }
                        .onSuccess {
                            trackEvent(
                                name = AnalyticsEventNames.PROGRESS_RESET_CONFIRMED,
                                params = emptyMap(),
                            )
                            _uiState.update { DeleteAllProgressUiState.Success }
                            _backUiAction.emit(Unit)
                        }.onFailure {
                            _uiState.update { DeleteAllProgressUiState.Idle }
                        }
                }
            }

            DeleteAllProgressUiEvent.OnCancel -> {
                trackEvent(
                    name = AnalyticsEventNames.PROGRESS_RESET_CANCELLED,
                    params = emptyMap(),
                )
                viewModelScope.launch {
                    _backUiAction.emit(Unit)
                }
            }
        }
    }
}
