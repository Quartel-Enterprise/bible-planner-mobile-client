package com.quare.bibleplanner.feature.logout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.logout.generated.resources.Res
import bibleplanner.feature.logout.generated.resources.logout_pending_favorites
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutFlushFailedException
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutUseCase
import com.quare.bibleplanner.feature.logout.presentation.mapper.LogoutErrorMapper
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutError
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiAction
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiEvent
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LogoutViewModel(
    private val logout: LogoutUseCase,
    private val logoutErrorMapper: LogoutErrorMapper,
) : ViewModel() {
    private val _uiState: MutableStateFlow<LogoutUiState> = MutableStateFlow(LogoutUiState.Idle)
    val uiState: StateFlow<LogoutUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<LogoutUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<LogoutUiAction> = _uiAction

    fun onEvent(event: LogoutUiEvent) {
        when (event) {
            LogoutUiEvent.OnConfirmLogout -> performLogout(shouldFlushPending = true)
            LogoutUiEvent.OnForceLogout -> performLogout(shouldFlushPending = false)
            LogoutUiEvent.OnCancel -> navigateBack()
            LogoutUiEvent.OnDismiss -> if (_uiState.value != LogoutUiState.Loading) navigateBack()
        }
    }

    private fun performLogout(shouldFlushPending: Boolean) {
        viewModelScope.launch {
            _uiState.update { LogoutUiState.Loading }
            logout(shouldFlushPending = shouldFlushPending)
                .onSuccess { _uiAction.emit(LogoutUiAction.NavigateBack) }
                .onFailure { throwable ->
                    if (throwable is LogoutFlushFailedException) {
                        _uiState.update { LogoutUiState.PendingChangesError(Res.string.logout_pending_favorites) }
                    } else {
                        _uiState.update { LogoutUiState.Idle }
                        _uiAction.emit(LogoutUiAction.ShowSnackbar(logoutErrorMapper.map(LogoutError.UNKNOWN)))
                    }
                }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiAction.emit(LogoutUiAction.NavigateBack)
        }
    }
}
