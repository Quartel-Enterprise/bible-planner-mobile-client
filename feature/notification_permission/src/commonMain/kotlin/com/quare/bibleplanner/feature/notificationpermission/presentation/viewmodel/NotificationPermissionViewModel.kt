package com.quare.bibleplanner.feature.notificationpermission.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiAction
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class NotificationPermissionViewModel(
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<NotificationPermissionUiEvent>(trackEvent) {
    private var hasRequestedSystemPermission = false

    val uiState: StateFlow<NotificationPermissionUiState>
        field = MutableStateFlow<NotificationPermissionUiState>(
            NotificationPermissionUiState(
                isFirstTime = true,
                shouldShowDismiss = true,
            ),
        )

    val uiAction: SharedFlow<NotificationPermissionUiAction>
        field = MutableSharedFlow<NotificationPermissionUiAction>()

    override fun handleEvent(event: NotificationPermissionUiEvent) {
        when (event) {
            NotificationPermissionUiEvent.OnConfirm -> handleConfirm()

            NotificationPermissionUiEvent.OnDecline -> navigator.navigateBack()

            is NotificationPermissionUiEvent.OnPermissionResult ->
                handlePermissionResult(event.granted, event.canAskAgain)
        }
    }

    private fun handleConfirm() {
        if (uiState.value.isFirstTime) {
            hasRequestedSystemPermission = true
            trackEvent(
                name = AnalyticsEventNames.NOTIFICATION_PERMISSION_PROMPTED,
                params = mapOf(AnalyticsParams.IS_FIRST_TIME to uiState.value.isFirstTime),
            )
            emit(NotificationPermissionUiAction.RequestSystemPermission)
        } else {
            viewModelScope.launch {
                uiAction.emit(NotificationPermissionUiAction.OpenNotificationSettings)
                // Why: the dialog only offers settings, so it closes once they open; the user returns to the app without it.
                navigator.navigateBack()
            }
        }
    }

    private fun handlePermissionResult(
        granted: Boolean,
        canAskAgain: Boolean,
    ) {
        if (hasRequestedSystemPermission) {
            hasRequestedSystemPermission = false
            trackEvent(
                name = AnalyticsEventNames.NOTIFICATION_PERMISSION_RESULT,
                params = mapOf(
                    AnalyticsParams.IS_GRANTED to granted,
                    AnalyticsParams.CAN_ASK_AGAIN to canAskAgain,
                ),
            )
        }
        if (!granted && !canAskAgain) {
            uiState.update {
                NotificationPermissionUiState(
                    isFirstTime = false,
                    shouldShowDismiss = false,
                )
            }
        } else {
            navigator.navigateBack()
        }
    }

    private fun emit(action: NotificationPermissionUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }
}
