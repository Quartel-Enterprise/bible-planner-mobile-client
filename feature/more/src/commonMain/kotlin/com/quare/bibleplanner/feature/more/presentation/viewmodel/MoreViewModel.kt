package com.quare.bibleplanner.feature.more.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.contact_support_email_body
import bibleplanner.feature.more.generated.resources.contact_support_email_subject
import bibleplanner.feature.more.generated.resources.login_requires_internet
import bibleplanner.feature.more.generated.resources.logout_requires_internet
import bibleplanner.feature.more.generated.resources.support_email_copied_message
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.model.legal.LegalUrl
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.GetWebAppUrl
import com.quare.bibleplanner.feature.more.domain.model.SupportContact
import com.quare.bibleplanner.feature.more.domain.usecase.GetInstagramUrlUseCase
import com.quare.bibleplanner.feature.more.presentation.factory.MoreUiStateFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction.OpenLink
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

internal class MoreViewModel(
    uiStateFactory: MoreUiStateFactory,
    private val calculateBibleProgress: CalculateBibleProgressUseCase,
    private val getWebAppUrl: GetWebAppUrl,
    private val getInstagramUrl: GetInstagramUrlUseCase,
    private val getAppStoreLink: GetAppStoreLinkUseCase,
    private val isConnected: IsConnected,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<MoreUiAction>()
    val uiAction: SharedFlow<MoreUiAction> = _uiAction
    private val _uiState = MutableStateFlow(uiStateFactory.initialState())
    val uiState: StateFlow<MoreUiState> = _uiState

    init {
        observe(uiStateFactory.create()) { state ->
            _uiState.value = state
        }
    }

    fun onEvent(event: MoreUiEvent) {
        when (event) {
            is MoreUiEvent.OnItemClick -> {
                when (event.type) {
                    MoreOptionItemType.THEME -> {
                        goToRoute(ThemeNavRoute)
                    }

                    MoreOptionItemType.APP_LANGUAGE -> {
                        goToRoute(AppLanguageNavRoute)
                    }

                    MoreOptionItemType.PRIVACY_POLICY -> {
                        emitAction(OpenLink(LegalUrl.PRIVACY_POLICY))
                    }

                    MoreOptionItemType.TERMS -> {
                        emitAction(OpenLink(LegalUrl.TERMS_OF_SERVICE))
                    }

                    MoreOptionItemType.BECOME_PRO -> {
                        goToRoute(PaywallNavRoute)
                    }

                    MoreOptionItemType.INSTAGRAM -> {
                        emitAction(OpenLink(getInstagramUrl()))
                    }

                    MoreOptionItemType.EDIT_PLAN_START_DAY -> {
                        goToRoute(EditPlanStartDateNavRoute)
                    }

                    MoreOptionItemType.DELETE_PROGRESS -> {
                        deleteProgressClick()
                    }

                    MoreOptionItemType.DONATE -> {
                        goToRoute(DonationNavRoute)
                    }

                    MoreOptionItemType.WEB_APP -> {
                        viewModelScope.launch {
                            emitAction(OpenLink(getWebAppUrl()))
                        }
                    }

                    MoreOptionItemType.RELEASE_NOTES -> {
                        goToRoute(ReleaseNotesNavRoute)
                    }

                    MoreOptionItemType.BIBLE_VERSION -> {
                        goToRoute(BibleVersionSelectorRoute)
                    }

                    MoreOptionItemType.CONTACT_SUPPORT -> {
                        _uiState.update { it.copy(showContactSupportDialog = true) }
                    }

                    MoreOptionItemType.RATE_APP -> {
                        emitAction(OpenLink(getAppStoreLink()))
                    }
                }
            }

            MoreUiEvent.OnProCardClick -> {
                _uiState.update { it.copy(showSubscriptionDetailsDialog = true) }
            }

            MoreUiEvent.OnDismissSubscriptionDetailsDialog -> {
                _uiState.update { it.copy(showSubscriptionDetailsDialog = false) }
            }

            MoreUiEvent.OnDismissContactSupportDialog -> {
                _uiState.update { it.copy(showContactSupportDialog = false) }
            }

            MoreUiEvent.OnSendSupportEmailClick -> {
                _uiState.update { it.copy(showContactSupportDialog = false) }
                sendSupportEmailClick()
            }

            MoreUiEvent.OnCopySupportEmailClick -> {
                viewModelScope.launch {
                    _uiAction.emit(MoreUiAction.Copy(SupportContact.EMAIL))
                    _uiAction.emit(MoreUiAction.ShowSnackbar(Res.string.support_email_copied_message))
                }
            }

            MoreUiEvent.OnLoginClick -> {
                navigateIfOnline(
                    route = LoginNavRoute(notifyResultViaSnackbar = false),
                    offlineMessage = Res.string.login_requires_internet,
                )
            }

            MoreUiEvent.OnLogoutClick -> {
                navigateIfOnline(
                    route = LogoutNavRoute,
                    offlineMessage = Res.string.logout_requires_internet,
                )
            }
        }
    }

    private fun sendSupportEmailClick() {
        viewModelScope.launch {
            val subject = getString(Res.string.contact_support_email_subject)
            val body = getString(Res.string.contact_support_email_body, _uiState.value.appVersion)
            val mailto = "mailto:${SupportContact.EMAIL}" +
                "?subject=${encodeMailtoComponent(subject)}" +
                "&body=${encodeMailtoComponent(body)}"
            _uiAction.emit(OpenLink(mailto))
        }
    }

    private fun encodeMailtoComponent(value: String): String = buildString {
        for (byte in value.encodeToByteArray()) {
            val unsigned = byte.toInt() and 0xFF
            val char = unsigned.toChar()
            if (unsigned < ASCII_LIMIT && (char.isLetterOrDigit() || char in UNRESERVED_CHARS)) {
                append(char)
            } else {
                append('%')
                append(unsigned.toString(radix = HEX_RADIX).uppercase().padStart(HEX_LENGTH, '0'))
            }
        }
    }

    private fun deleteProgressClick() {
        viewModelScope.launch {
            val progress = calculateBibleProgress().first()
            _uiAction.emit(
                if (progress > 0) {
                    MoreUiAction.GoToRoute(DeleteAllProgressNavRoute)
                } else {
                    MoreUiAction.ShowNoProgressToDelete
                },
            )
        }
    }

    private fun navigateIfOnline(
        route: Any,
        offlineMessage: StringResource,
    ) {
        viewModelScope.launch {
            val action = if (isConnected()) {
                MoreUiAction.GoToRoute(route)
            } else {
                MoreUiAction.ShowSnackbar(offlineMessage)
            }
            _uiAction.emit(action)
        }
    }

    private fun goToRoute(route: Any) {
        emitAction(MoreUiAction.GoToRoute(route))
    }

    private fun emitAction(action: MoreUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    private companion object {
        const val ASCII_LIMIT = 128
        const val UNRESERVED_CHARS = "-_.~"
        const val HEX_RADIX = 16
        const val HEX_LENGTH = 2
    }
}
