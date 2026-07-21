package com.quare.bibleplanner.feature.profile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.login_requires_internet
import bibleplanner.feature.profile.generated.resources.logout_requires_internet
import bibleplanner.feature.profile.generated.resources.up_to_date_message
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.model.legal.LegalUrl
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.EditProfileNavRoute
import com.quare.bibleplanner.core.model.route.ExpandedPhotoNavRoute
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.GetWebAppUrl
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.profile.domain.usecase.GetInstagramUrlUseCase
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileUiStateFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiAction
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiAction.OpenLink
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

internal class ProfileViewModel(
    uiStateFactory: ProfileUiStateFactory,
    private val calculateBibleProgress: CalculateBibleProgressUseCase,
    private val getWebAppUrl: GetWebAppUrl,
    private val getInstagramUrl: GetInstagramUrlUseCase,
    private val getAppStoreLink: GetAppStoreLinkUseCase,
    private val isConnected: IsConnected,
    private val checkForUpdate: CheckForUpdate,
    trackEvent: TrackEvent,
) : TrackedViewModel<ProfileUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<ProfileUiAction>()
    val uiAction: SharedFlow<ProfileUiAction> = _uiAction
    private val isCheckingForUpdate = MutableStateFlow(false)
    val uiState: StateFlow<ProfileUiState> = combine(
        uiStateFactory.create(),
        isCheckingForUpdate,
    ) { state, checking ->
        state.copy(isCheckingForUpdate = checking)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = uiStateFactory.initialState(),
    )

    override fun handleEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.OnItemClick -> {
                when (event.type) {
                    ProfileOptionItemType.THEME -> goToRoute(ThemeNavRoute)

                    ProfileOptionItemType.APP_LANGUAGE -> goToRoute(AppLanguageNavRoute)

                    ProfileOptionItemType.PRIVACY_POLICY -> emitAction(OpenLink(LegalUrl.PRIVACY_POLICY))

                    ProfileOptionItemType.TERMS -> emitAction(OpenLink(LegalUrl.TERMS_OF_SERVICE))

                    ProfileOptionItemType.BECOME_PRO -> {
                        trackEvent(
                            name = AnalyticsEventNames.PAYWALL_VIEWED,
                            params = mapOf(AnalyticsParams.SOURCE to PAYWALL_SOURCE),
                        )
                        goToRoute(PaywallNavRoute)
                    }

                    ProfileOptionItemType.INSTAGRAM -> emitAction(OpenLink(getInstagramUrl()))

                    ProfileOptionItemType.EDIT_PLAN_START_DAY -> goToRoute(EditPlanStartDateNavRoute)

                    ProfileOptionItemType.DELETE_PROGRESS -> deleteProgressClick()

                    ProfileOptionItemType.DONATE -> goToRoute(DonationNavRoute)

                    ProfileOptionItemType.WEB_APP -> {
                        viewModelScope.launch {
                            emitAction(OpenLink(getWebAppUrl()))
                        }
                    }

                    ProfileOptionItemType.RELEASE_NOTES -> goToRoute(ReleaseNotesNavRoute)

                    ProfileOptionItemType.BIBLE_VERSION -> goToRoute(BibleVersionSelectorRoute)

                    ProfileOptionItemType.CONTACT_SUPPORT -> goToRoute(ContactSupportNavRoute)

                    ProfileOptionItemType.RATE_APP -> rateAppClick()

                    ProfileOptionItemType.CHECK_FOR_UPDATE -> checkForUpdateClick()
                }
            }

            ProfileUiEvent.OnProCardClick -> goToRoute(SubscriptionDetailsNavRoute)

            ProfileUiEvent.OnAccountCardClick -> goToRoute(AccountDetailsNavRoute)

            ProfileUiEvent.OnEditProfileClick -> goToRoute(EditProfileNavRoute)

            ProfileUiEvent.OnAvatarClick -> goToRoute(ExpandedPhotoNavRoute)

            ProfileUiEvent.OnLoginClick -> {
                navigateIfOnline(
                    route = LoginNavRoute(notifyResultViaSnackbar = false),
                    offlineMessage = Res.string.login_requires_internet,
                )
            }

            ProfileUiEvent.OnLogoutClick -> {
                navigateIfOnline(
                    route = LogoutNavRoute,
                    offlineMessage = Res.string.logout_requires_internet,
                )
            }
        }
    }

    private fun rateAppClick() {
        viewModelScope.launch {
            emitAction(OpenLink(getAppStoreLink()))
        }
    }

    private fun checkForUpdateClick() {
        if (isCheckingForUpdate.value) return
        isCheckingForUpdate.value = true
        viewModelScope.launch {
            val availability = checkForUpdate()
            isCheckingForUpdate.value = false
            val action = when (availability) {
                is UpdateAvailability.Available ->
                    ProfileUiAction.GoToRoute(
                        InAppUpdateNavRoute(
                            versionName = availability.versionName,
                            source = UpdatePromptSource.MANUAL,
                        ),
                    )

                UpdateAvailability.NotAvailable ->
                    ProfileUiAction.ShowSnackbar(Res.string.up_to_date_message)
            }
            _uiAction.emit(action)
        }
    }

    private fun deleteProgressClick() {
        viewModelScope.launch {
            val progress = calculateBibleProgress().first()
            _uiAction.emit(
                if (progress > 0) {
                    ProfileUiAction.GoToRoute(DeleteAllProgressNavRoute)
                } else {
                    ProfileUiAction.ShowNoProgressToDelete
                },
            )
        }
    }

    private fun navigateIfOnline(
        route: NavKey,
        offlineMessage: StringResource,
    ) {
        viewModelScope.launch {
            val action = if (isConnected()) {
                ProfileUiAction.GoToRoute(route)
            } else {
                ProfileUiAction.ShowSnackbar(offlineMessage)
            }
            _uiAction.emit(action)
        }
    }

    private fun goToRoute(route: NavKey) {
        emitAction(ProfileUiAction.GoToRoute(route))
    }

    private fun emitAction(action: ProfileUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    private companion object {
        const val PAYWALL_SOURCE = "profile_menu"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
