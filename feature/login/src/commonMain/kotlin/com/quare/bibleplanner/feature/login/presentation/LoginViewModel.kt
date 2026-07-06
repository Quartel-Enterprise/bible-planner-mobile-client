package com.quare.bibleplanner.feature.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.login_result_error
import bibleplanner.feature.login.generated.resources.login_result_success
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
    private val signInStarter: SignInStarter,
    supabaseClient: SupabaseClient,
    observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    uiStateFactory: LoginUiStateFactory,
    private val throwableToLoginErrorMapper: ThrowableToLoginErrorMapper,
    private val noGoogleAccountClassifier: NoGoogleAccountClassifier,
    private val addGoogleAccountLauncher: AddGoogleAccountLauncher,
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

            is LoginUiEvent.SocialLoginClick -> {
                _state.update { it.copy(loadingProvider = uiEvent.provider, error = null) }
                viewModelScope.launch {
                    signInStarter(uiEvent.provider, uiEvent.nativeSignInState).onFailure { throwable ->
                        _state.update {
                            it.copy(
                                loadingProvider = null,
                                error = throwableToLoginErrorMapper(throwable),
                            )
                        }
                    }
                }
            }

            is LoginUiEvent.SocialAuthResult -> {
                _state.update {
                    when (val result = uiEvent.result) {
                        is NativeSignInResult.Success -> it

                        is NativeSignInResult.ClosedByUser -> it.copy(loadingProvider = null)

                        is NativeSignInResult.NetworkError -> it.copy(
                            loadingProvider = null,
                            error = LoginError.CONNECTION,
                        )

                        is NativeSignInResult.Error -> if (noGoogleAccountClassifier(result.exception)) {
                            it.copy(loadingProvider = null, showAddGoogleAccountDialog = true)
                        } else {
                            it.copy(
                                loadingProvider = null,
                                error = throwableToLoginErrorMapper(result.exception),
                            )
                        }
                    }
                }
                notifyLoginResult(uiEvent.result)
            }

            LoginUiEvent.NotNowClick -> {
                viewModelScope.launch {
                    close()
                }
            }

            LoginUiEvent.AddGoogleAccountConfirmClick -> {
                addGoogleAccountLauncher()
                _state.update { it.copy(showAddGoogleAccountDialog = false) }
            }

            LoginUiEvent.DismissAddGoogleAccountDialog -> {
                _state.update { it.copy(showAddGoogleAccountDialog = false) }
            }
        }
    }

    private fun notifyLoginResult(result: NativeSignInResult) {
        val message = when (result) {
            is NativeSignInResult.Success -> Res.string.login_result_success

            is NativeSignInResult.NetworkError -> Res.string.login_result_error

            is NativeSignInResult.Error -> if (noGoogleAccountClassifier(result.exception)) {
                null
            } else {
                Res.string.login_result_error
            }

            is NativeSignInResult.ClosedByUser -> null
        } ?: return
        viewModelScope.launch {
            _uiAction.emit(LoginUiAction.NotifyLoginResult(message))
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
