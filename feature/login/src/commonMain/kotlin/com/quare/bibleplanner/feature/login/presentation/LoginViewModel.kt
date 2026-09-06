package com.quare.bibleplanner.feature.login.presentation

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.login_result_error
import bibleplanner.feature.login.generated.resources.login_result_success
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.factory.LoginUiStateFactory
import com.quare.bibleplanner.feature.login.presentation.mapper.ThrowableToLoginErrorMapper
import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiState
import com.quare.bibleplanner.ui.utils.observe
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
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
    private val isGoogleCredentialUnavailable: IsGoogleCredentialUnavailable,
    private val addGoogleAccountLauncher: AddGoogleAccountLauncher,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<LoginUiEvent>(trackEvent) {
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

    override fun handleEvent(uiEvent: LoginUiEvent) {
        when (uiEvent) {
            LoginUiEvent.DismissClick -> navigator.navigateBack()

            is LoginUiEvent.SocialLoginClick -> {
                _state.update { it.copy(loadingProvider = uiEvent.provider, error = null) }
                viewModelScope.launch {
                    signInStarter(uiEvent.provider, uiEvent.nativeSignInState).onFailure { throwable ->
                        val error = throwableToLoginErrorMapper(throwable)
                        trackLoginFailed(
                            provider = uiEvent.provider,
                            reason = error.reasonParam,
                        )
                        _state.update {
                            it.copy(
                                loadingProvider = null,
                                error = error,
                            )
                        }
                    }
                }
            }

            is LoginUiEvent.SocialAuthResult -> {
                trackAuthResult(uiEvent)
                _state.update {
                    when (val result = uiEvent.result) {
                        is NativeSignInResult.Success -> it

                        else -> it.copy(
                            loadingProvider = null,
                            error = findLoginErrorOrNull(result),
                            showGoogleSignInUnavailableDialog = result.hasNoGoogleCredential(),
                        )
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
                _state.update { it.copy(showGoogleSignInUnavailableDialog = false) }
            }

            LoginUiEvent.DismissAddGoogleAccountDialog ->
                _state.update { it.copy(showGoogleSignInUnavailableDialog = false) }
        }
    }

    private fun trackAuthResult(uiEvent: LoginUiEvent.SocialAuthResult) {
        when (val result = uiEvent.result) {
            is NativeSignInResult.Success -> trackLoginEvent(
                name = AnalyticsEventNames.LOGIN,
                provider = uiEvent.provider,
            )

            is NativeSignInResult.ClosedByUser -> trackLoginEvent(
                name = AnalyticsEventNames.LOGIN_CANCELLED,
                provider = uiEvent.provider,
            )

            else -> trackLoginFailed(
                provider = uiEvent.provider,
                reason = findLoginErrorOrNull(result)?.reasonParam ?: GOOGLE_UNAVAILABLE_REASON,
            )
        }
    }

    /**
     * The message to show for [result], or `null` when there is nothing to show — the user signed
     * in, closed the sheet, or is being offered the Google-unavailable dialog instead.
     */
    private fun findLoginErrorOrNull(result: NativeSignInResult): LoginError? = when (result) {
        is NativeSignInResult.Success, is NativeSignInResult.ClosedByUser -> null

        is NativeSignInResult.NetworkError -> LoginError.CONNECTION

        is NativeSignInResult.Error -> if (isGoogleCredentialUnavailable(result.exception)) {
            null
        } else {
            throwableToLoginErrorMapper(result.exception)
        }
    }

    private fun NativeSignInResult.hasNoGoogleCredential(): Boolean =
        this is NativeSignInResult.Error && isGoogleCredentialUnavailable(exception)

    private fun trackLoginEvent(
        name: String,
        provider: LoginProvider,
    ) {
        trackEvent(
            name = name,
            params = mapOf(AnalyticsParams.METHOD to provider.methodParam),
        )
    }

    private fun trackLoginFailed(
        provider: LoginProvider,
        reason: String,
    ) {
        trackEvent(
            name = AnalyticsEventNames.LOGIN_FAILED,
            params = mapOf(
                AnalyticsParams.METHOD to provider.methodParam,
                AnalyticsParams.REASON to reason,
            ),
        )
    }

    private val LoginProvider.methodParam: String
        get() = name.lowercase()

    private val LoginError.reasonParam: String
        get() = name.lowercase()

    private fun notifyLoginResult(result: NativeSignInResult) {
        val message = when {
            result is NativeSignInResult.Success -> Res.string.login_result_success
            findLoginErrorOrNull(result) != null -> Res.string.login_result_error
            else -> null
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

    private fun navigateBack() {
        navigator.navigateBack()
    }

    private companion object {
        /** Reason reported when the platform could not provide a Google credential at all. */
        const val GOOGLE_UNAVAILABLE_REASON = "google_unavailable"
    }
}
