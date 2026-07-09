package com.quare.bibleplanner.feature.contactsupport.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.contact_support.generated.resources.Res
import bibleplanner.feature.contact_support.generated.resources.support_email_copied_message
import com.quare.bibleplanner.feature.contactsupport.domain.model.SupportContact
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.ContactSupportMailtoFactory
import com.quare.bibleplanner.feature.contactsupport.presentation.factory.ContactSupportUiStateFactory
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiAction
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiEvent
import com.quare.bibleplanner.feature.contactsupport.presentation.model.ContactSupportUiState
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class ContactSupportViewModel(
    uiStateFactory: ContactSupportUiStateFactory,
    private val mailtoFactory: ContactSupportMailtoFactory,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<ContactSupportUiAction>()
    val uiAction: SharedFlow<ContactSupportUiAction> = _uiAction
    private val _uiState = MutableStateFlow(uiStateFactory.initialState())
    val uiState: StateFlow<ContactSupportUiState> = _uiState

    init {
        observe(uiStateFactory.create()) { state ->
            _uiState.value = state
        }
    }

    fun onEvent(event: ContactSupportUiEvent) {
        when (event) {
            ContactSupportUiEvent.OnDismiss -> {
                emitAction(ContactSupportUiAction.NavigateBack)
            }

            ContactSupportUiEvent.OnSendEmailClick -> {
                sendSupportEmailClick()
            }

            ContactSupportUiEvent.OnCopyEmailClick -> {
                viewModelScope.launch {
                    _uiAction.emit(ContactSupportUiAction.Copy(SupportContact.EMAIL))
                    _uiAction.emit(ContactSupportUiAction.ShowSnackbar(Res.string.support_email_copied_message))
                }
            }
        }
    }

    private fun sendSupportEmailClick() {
        viewModelScope.launch {
            val mailto = mailtoFactory.create(_uiState.value)
            _uiAction.emit(ContactSupportUiAction.OpenLink(mailto))
        }
    }

    private fun emitAction(action: ContactSupportUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }
}
