package com.quare.bibleplanner.feature.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.login.presentation.factory.LoginUiStateFactory
import com.quare.bibleplanner.feature.login.presentation.mapper.ThrowableToLoginErrorMapper
import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiState
import com.quare.bibleplanner.ui.utils.observe
import io.github.jan.supabase.SupabaseClient
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
    private val appleSignInStarter: AppleSignInStarter,
    supabaseClient: SupabaseClient,
    observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    uiStateFactory: LoginUiStateFactory,
    private val throwableToLoginErrorMapper: ThrowableToLoginErrorMapper,
) : ViewModel() {
    val composeAuth: ComposeAuth = supabaseClient.composeAuth
    private val _state: MutableStateFlow<LoginUiState> = MutableStateFlow(uiStateFactory.create())
    val state: StateFlow<LoginUiState> = _state

    private val _uiAction: MutableSharedFlow<LoginUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<LoginUiAction> = _uiAction

    init {
        observe(observeAuthenticatedUserId()) { userId ->
            if (userId != null) {
                close()
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

            is LoginUiEvent.SocialLoginClick.Google -> {
                _state.update { it.copy(isGoogleLoading = true, error = null) }
                viewModelScope.launch {
                    googleSignInStarter(uiEvent.nativeSignInState).onFailure { throwable ->
                        _state.update {
                            it.copy(
                                isGoogleLoading = false,
                                error = throwableToLoginErrorMapper(throwable),
                            )
                        }
                    }
                }
            }

            is LoginUiEvent.SocialLoginClick.Apple -> {
                _state.update { it.copy(isAppleLoading = true, error = null) }
                viewModelScope.launch {
                    appleSignInStarter(uiEvent.nativeSignInState).onFailure { throwable ->
                        _state.update {
                            it.copy(
                                isAppleLoading = false,
                                error = throwableToLoginErrorMapper(throwable),
                            )
                        }
                    }
                }
            }

            is LoginUiEvent.GoogleAuthResult -> {
                _state.update {
                    when (val result = uiEvent.result) {
                        is NativeSignInResult.Success -> it

                        is NativeSignInResult.ClosedByUser -> it.copy(isGoogleLoading = false)

                        is NativeSignInResult.NetworkError -> it.copy(
                            isGoogleLoading = false,
                            error = LoginError.CONNECTION,
                        )

                        is NativeSignInResult.Error -> it.copy(
                            isGoogleLoading = false,
                            error = throwableToLoginErrorMapper(result.exception),
                        )
                    }
                }
            }

            is LoginUiEvent.AppleAuthResult -> {
                _state.update {
                    when (val result = uiEvent.result) {
                        is NativeSignInResult.Success -> it

                        is NativeSignInResult.ClosedByUser -> it.copy(isAppleLoading = false)

                        is NativeSignInResult.NetworkError -> it.copy(
                            isAppleLoading = false,
                            error = LoginError.CONNECTION,
                        )

                        is NativeSignInResult.Error -> it.copy(
                            isAppleLoading = false,
                            error = throwableToLoginErrorMapper(result.exception),
                        )
                    }
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
