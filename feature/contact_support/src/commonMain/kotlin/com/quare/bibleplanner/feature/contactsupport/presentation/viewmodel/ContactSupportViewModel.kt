package com.quare.bibleplanner.feature.contactsupport.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.contact_support.generated.resources.Res
import bibleplanner.feature.contact_support.generated.resources.support_email_copied_message
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.feature.contactsupport.domain.model.AccountStatusModel
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
    private val trackEvent: TrackEvent,
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
            ContactSupportUiEvent.OnDismiss -> emitAction(ContactSupportUiAction.NavigateBack)

            ContactSupportUiEvent.OnSendEmailClick -> sendSupportEmailClick()

            ContactSupportUiEvent.OnCopyEmailClick -> {
                trackEvent(
                    name = AnalyticsEventNames.CONTACT_SUPPORT_EMAIL_COPIED,
                    params = emptyMap(),
                )
                viewModelScope.launch {
                    _uiAction.emit(ContactSupportUiAction.Copy(SupportContact.EMAIL))
                    _uiAction.emit(ContactSupportUiAction.ShowSnackbar(Res.string.support_email_copied_message))
                }
            }
        }
    }

    private fun sendSupportEmailClick() {
        val state = _uiState.value
        trackEvent(
            name = AnalyticsEventNames.CONTACT_SUPPORT_EMAIL_OPENED,
            params = buildMap {
                state.accountStatusModel.toIsLoggedIn()?.let { put(AnalyticsParams.IS_LOGGED_IN, it) }
                state.subscriptionStatus.toIsPro()?.let { put(AnalyticsParams.IS_PRO, it) }
            },
        )
        viewModelScope.launch {
            val mailto = mailtoFactory.create(state)
            _uiAction.emit(ContactSupportUiAction.OpenLink(mailto))
        }
    }

    private fun AccountStatusModel.toIsLoggedIn(): Boolean? = when (this) {
        is AccountStatusModel.LoggedIn -> true
        AccountStatusModel.LoggedOut -> false
        AccountStatusModel.Loading, AccountStatusModel.Error -> null
    }

    private fun Loadable<SubscriptionStatus?>.toIsPro(): Boolean? =
        (this as? Loadable.Loaded)?.let { loaded -> loaded.value is SubscriptionStatus.Pro }

    private fun emitAction(action: ContactSupportUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }
}
