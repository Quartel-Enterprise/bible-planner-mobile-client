package com.quare.bibleplanner.feature.logout.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.logout.domain.usecase.Logout
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutFlushFailedException
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutProgress
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutError
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiAction
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiEvent
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal class LogoutViewModel(
    private val logout: Logout,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<LogoutUiEvent>(trackEvent) {
    val uiState: StateFlow<LogoutUiState>
        field = MutableStateFlow<LogoutUiState>(LogoutUiState.Idle)

    val uiAction: SharedFlow<LogoutUiAction>
        field = MutableSharedFlow<LogoutUiAction>()

    override fun handleEvent(event: LogoutUiEvent) {
        when (event) {
            is LogoutUiEvent.ConfirmLogoutClick -> performLogout(shouldFlushPending = event.shouldFlushPending)
            LogoutUiEvent.OnCancel -> navigator.navigateBack()
            LogoutUiEvent.OnDismiss -> if (uiState.value !is LogoutUiState.Loading) navigator.navigateBack()
        }
    }

    private fun performLogout(shouldFlushPending: Boolean) {
        logout(shouldFlushPending)
            .onEach { progress ->
                when (progress) {
                    is LogoutProgress.InProgress -> uiState.update { LogoutUiState.Loading(progress.phase) }
                    is LogoutProgress.Finished -> progress.result.handleLogout()
                }
            }.launchIn(viewModelScope)
    }

    private suspend fun Result<Unit>.handleLogout() {
        onSuccess {
            uiAction.emit(LogoutUiAction.NotifySuccess)
            navigator.navigateBack()
        }.onFailure { throwable ->
            if (throwable is LogoutFlushFailedException) {
                trackLogoutFailed(reason = REASON_PENDING_CHANGES)
                uiState.update { LogoutUiState.PendingChangesError }
            } else {
                trackLogoutFailed(reason = REASON_UNKNOWN)
                uiState.update { LogoutUiState.Idle }
                uiAction.emit(LogoutUiAction.ShowError(LogoutError.UNKNOWN))
            }
        }
    }

    private fun trackLogoutFailed(reason: String) {
        trackEvent(
            name = AnalyticsEventNames.LOGOUT_FAILED,
            params = mapOf(AnalyticsParams.REASON to reason),
        )
    }

    private companion object {
        const val REASON_PENDING_CHANGES = "pending_changes"
        const val REASON_UNKNOWN = "unknown"
    }
}
