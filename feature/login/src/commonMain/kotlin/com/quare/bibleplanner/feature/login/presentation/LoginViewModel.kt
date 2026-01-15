package com.quare.bibleplanner.feature.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiAction
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _uiAction: MutableSharedFlow<LoginUiAction> = MutableSharedFlow()
    val uiAction: SharedFlow<LoginUiAction> = _uiAction

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            LoginUiEvent.Dismiss -> viewModelScope.launch {
                _uiAction.emit(LoginUiAction.Dismiss)
            }
        }
    }
}
