package com.quare.bibleplanner.feature.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.login.presentation.factory.LoginUiStateFactory
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiState
import com.quare.bibleplanner.ui.utils.observe
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class LoginViewModel(
    private val googleSignInStarter: GoogleSignInStarter,
    supabaseClient: SupabaseClient,
    uiStateFactory: LoginUiStateFactory,
) : ViewModel() {
    val composeAuth: ComposeAuth = supabaseClient.composeAuth
    private val _state: MutableStateFlow<LoginUiState> = MutableStateFlow(uiStateFactory.create())
    val state: StateFlow<LoginUiState> = _state

    private val _uiAction: MutableSharedFlow<LoginUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<LoginUiAction> = _uiAction

    init {
        observe(supabaseClient.auth.sessionStatus) { sessionStatus ->
            when (sessionStatus) {
                is SessionStatus.Authenticated -> close()
                SessionStatus.Initializing -> Unit
                is SessionStatus.NotAuthenticated -> Unit
                is SessionStatus.RefreshFailure -> Unit
            }
        }
    }

    fun onEvent(uiEvent: LoginUiEvent) {
        when (uiEvent) {
            LoginUiEvent.DismissClick -> {
                viewModelScope.launch {
                    navigateBack()
                }
            }

            is LoginUiEvent.SocialLoginClick -> {
                if (uiEvent is LoginUiEvent.SocialLoginClick.Google) {
                    _state.update { it.copy(isGoogleLoading = true, isErrorVisible = false) }
                    viewModelScope.launch {
                        googleSignInStarter(uiEvent.nativeSignInState).onFailure {
                            _state.update { it.copy(isGoogleLoading = false, isErrorVisible = true) }
                        }
                    }
                } else {
                    uiEvent.nativeSignInState.startFlow()
                }
            }

            is LoginUiEvent.GoogleAuthResult -> {
                if (uiEvent.result is NativeSignInResult.ClosedByUser) {
                    _state.update { it.copy(isGoogleLoading = false) }
                }
            }

            LoginUiEvent.NotNowClick -> {
                viewModelScope.launch {
                    close()
                }
            }
        }
    }

    private suspend fun close() {
        _uiAction.emit(LoginUiAction.CloseBottomSheet)
        delay(250.milliseconds)
        navigateBack()
    }

    private suspend fun navigateBack() {
        _uiAction.emit(LoginUiAction.NavigateBack)
    }
}
