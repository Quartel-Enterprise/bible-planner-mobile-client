package com.quare.bibleplanner.feature.loginwarning.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiAction
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class LoginWarningViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val reason: LoginWarningReason = LoginWarningReason.fromKey(
        savedStateHandle.toRoute<LoginWarningNavRoute>().reason,
    )

    private val _uiAction: MutableSharedFlow<LoginWarningUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<LoginWarningUiAction> = _uiAction

    fun onEvent(event: LoginWarningUiEvent) {
        viewModelScope.launch {
            _uiAction.emit(
                when (event) {
                    LoginWarningUiEvent.OnLoginClick -> LoginWarningUiAction.NavigateToLogin
                    LoginWarningUiEvent.OnDismiss -> LoginWarningUiAction.NavigateBack
                },
            )
        }
    }
}
