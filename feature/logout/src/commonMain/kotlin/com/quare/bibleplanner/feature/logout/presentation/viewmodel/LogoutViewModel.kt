package com.quare.bibleplanner.feature.logout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiEvent
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LogoutViewModel(
    private val supabaseClient: SupabaseClient,
) : ViewModel() {
    private val _uiState: MutableStateFlow<LogoutUiState> = MutableStateFlow(LogoutUiState.Idle)
    val uiState: StateFlow<LogoutUiState> = _uiState.asStateFlow()

    private val _backUiAction: MutableSharedFlow<Unit> = MutableSharedFlow()
    val backUiAction: SharedFlow<Unit> = _backUiAction

    fun onEvent(event: LogoutUiEvent) {
        when (event) {
            LogoutUiEvent.OnConfirmLogout -> {
                viewModelScope.launch {
                    _uiState.update { LogoutUiState.Loading }
                    try {
                        supabaseClient.auth.signOut()
                        _backUiAction.emit(Unit)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        _uiState.update { LogoutUiState.Idle }
                    }
                }
            }

            LogoutUiEvent.OnCancel -> {
                viewModelScope.launch {
                    _backUiAction.emit(Unit)
                }
            }

            LogoutUiEvent.OnDismiss -> {
                if (_uiState.value != LogoutUiState.Loading) {
                    viewModelScope.launch {
                        _backUiAction.emit(Unit)
                    }
                }
            }
        }
    }
}
