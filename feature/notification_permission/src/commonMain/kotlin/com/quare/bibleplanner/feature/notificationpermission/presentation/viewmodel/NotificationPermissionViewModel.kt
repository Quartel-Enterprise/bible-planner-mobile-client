package com.quare.bibleplanner.feature.notificationpermission.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.notification_permission.generated.resources.Res
import bibleplanner.feature.notification_permission.generated.resources.notification_permission_accept
import bibleplanner.feature.notification_permission.generated.resources.notification_permission_explanation
import bibleplanner.feature.notification_permission.generated.resources.notification_permission_open_settings
import bibleplanner.feature.notification_permission.generated.resources.notification_permission_settings_message
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiAction
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class NotificationPermissionViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<NotificationPermissionUiState> =
        MutableStateFlow(
            NotificationPermissionUiState(
                isFirstTime = true,
                textRes = Res.string.notification_permission_explanation,
                confirmButtonTextRes = Res.string.notification_permission_accept,
                shouldShowDismiss = true,
            ),
        )
    val uiState: StateFlow<NotificationPermissionUiState> = _uiState.asStateFlow()

    private val _uiAction: MutableSharedFlow<NotificationPermissionUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<NotificationPermissionUiAction> = _uiAction

    fun onEvent(event: NotificationPermissionUiEvent) {
        when (event) {
            NotificationPermissionUiEvent.OnConfirm -> {
                handleConfirm()
            }

            NotificationPermissionUiEvent.OnDecline -> {
                emit(NotificationPermissionUiAction.NavigateBack)
            }

            is NotificationPermissionUiEvent.OnPermissionResult -> {
                handlePermissionResult(event.granted, event.canAskAgain)
            }
        }
    }

    private fun handleConfirm() {
        emit(
            if (_uiState.value.isFirstTime) {
                NotificationPermissionUiAction.RequestSystemPermission
            } else {
                NotificationPermissionUiAction.OpenNotificationSettings
            },
        )
    }

    private fun handlePermissionResult(
        granted: Boolean,
        canAskAgain: Boolean,
    ) {
        if (!granted && !canAskAgain) {
            _uiState.update {
                NotificationPermissionUiState(
                    isFirstTime = false,
                    textRes = Res.string.notification_permission_settings_message,
                    confirmButtonTextRes = Res.string.notification_permission_open_settings,
                    shouldShowDismiss = false,
                )
            }
        } else {
            emit(NotificationPermissionUiAction.NavigateBack)
        }
    }

    private fun emit(action: NotificationPermissionUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }
}
