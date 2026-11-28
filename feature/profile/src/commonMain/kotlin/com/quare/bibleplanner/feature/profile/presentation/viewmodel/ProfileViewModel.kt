package com.quare.bibleplanner.feature.profile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.core.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.core.inappupdate.domain.usecase.ShowUpdatePrompt
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.legal.LegalUrl
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute
import com.quare.bibleplanner.core.model.route.AnnotationsNavRoute
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAccountNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.EditProfileNavRoute
import com.quare.bibleplanner.core.model.route.ExpandedPhotoNavRoute
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.StudySuggestionNavRoute
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentService
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.feature.profile.domain.usecase.GetInstagramUrlUseCase
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileUiStateFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileMessage
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiAction
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiAction.OpenLink
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import software.quare.connectivity.IsConnected
import kotlin.time.Duration.Companion.seconds

class ProfileViewModel internal constructor(
    private val calculateBibleProgress: CalculateBibleProgressUseCase,
    private val getInstagramUrl: GetInstagramUrlUseCase,
    private val getAppStoreLink: GetAppStoreLinkUseCase,
    private val isConnected: IsConnected,
    private val checkForUpdate: CheckForUpdate,
    private val showUpdatePrompt: ShowUpdatePrompt,
    private val adsConsentService: AdsConsentService,
    private val navigator: Navigator,
    uiStateFactory: ProfileUiStateFactory,
    trackEvent: TrackEvent,
) : TrackedViewModel<ProfileUiEvent>(trackEvent) {
    val uiAction: SharedFlow<ProfileUiAction>
        field = MutableSharedFlow<ProfileUiAction>()
    private val isCheckingForUpdate = MutableStateFlow(false)
    private val externalActionJobs = mutableMapOf<ProfileOptionItemType, Job>()
    private val linkRepeatGuard = 1.seconds
    private val privacyOptionsRepeatGuard = 5.seconds
    val uiState: StateFlow<ProfileUiState> = flow {
        val reducedState = combine(
            uiStateFactory.create(initialState = uiState.value),
            isCheckingForUpdate,
        ) { state, checking ->
            state.copy(isCheckingForUpdate = checking)
        }
        emitAll(reducedState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = uiStateFactory.createInitialState(),
    )

    override fun handleEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.OnItemClick -> {
                when (event.type) {
                    ProfileOptionItemType.THEME -> goToRoute(ThemeNavRoute)

                    ProfileOptionItemType.APP_LANGUAGE -> goToRoute(AppLanguageNavRoute)

                    ProfileOptionItemType.STUDY_SUGGESTION -> goToRoute(StudySuggestionNavRoute)

                    ProfileOptionItemType.PRIVACY_POLICY -> openLink(event.type, LegalUrl.PRIVACY_POLICY)

                    ProfileOptionItemType.TERMS -> openLink(event.type, LegalUrl.TERMS_OF_SERVICE)

                    ProfileOptionItemType.PRIVACY_OPTIONS -> privacyOptionsClick()

                    ProfileOptionItemType.BECOME_PRO ->
                        goToRoute(PaywallNavRoute(PaywallEntrySource.PROFILE_MENU))

                    ProfileOptionItemType.INSTAGRAM -> openLink(event.type, getInstagramUrl())

                    ProfileOptionItemType.EDIT_PLAN_START_DAY -> goToRoute(EditPlanStartDateNavRoute)

                    ProfileOptionItemType.DELETE_PROGRESS -> deleteProgressClick()

                    ProfileOptionItemType.DELETE_ACCOUNT -> {
                        navigateIfOnline(
                            route = DeleteAccountNavRoute,
                            offlineMessage = ProfileMessage.DELETE_ACCOUNT_REQUIRES_INTERNET,
                        )
                    }

                    ProfileOptionItemType.DONATE -> goToRoute(DonationNavRoute)

                    ProfileOptionItemType.RELEASE_NOTES -> goToRoute(ReleaseNotesNavRoute)

                    ProfileOptionItemType.BIBLE_VERSION -> goToRoute(BibleVersionSelectorRoute)

                    ProfileOptionItemType.CONTACT_SUPPORT -> goToRoute(ContactSupportNavRoute)

                    ProfileOptionItemType.RATE_APP -> openLink(event.type, getAppStoreLink())

                    ProfileOptionItemType.CHECK_FOR_UPDATE -> checkForUpdateClick()

                    ProfileOptionItemType.ANNOTATIONS -> goToRoute(AnnotationsNavRoute)
                }
            }

            ProfileUiEvent.OnProCardClick -> goToRoute(SubscriptionDetailsNavRoute)

            ProfileUiEvent.OnAccountCardClick -> goToRoute(AccountDetailsNavRoute)

            ProfileUiEvent.OnEditProfileClick -> goToRoute(EditProfileNavRoute)

            ProfileUiEvent.OnAvatarClick -> goToRoute(ExpandedPhotoNavRoute)

            ProfileUiEvent.OnLoginClick -> {
                navigateIfOnline(
                    route = LoginNavRoute(notifyResultViaSnackbar = false),
                    offlineMessage = ProfileMessage.LOGIN_REQUIRES_INTERNET,
                )
            }

            ProfileUiEvent.OnLogoutClick -> {
                navigateIfOnline(
                    route = LogoutNavRoute,
                    offlineMessage = ProfileMessage.LOGOUT_REQUIRES_INTERNET,
                )
            }
        }
    }

    private fun checkForUpdateClick() {
        if (isCheckingForUpdate.value) return
        isCheckingForUpdate.value = true
        viewModelScope.launch {
            val availability = checkForUpdate()
            isCheckingForUpdate.value = false
            when (availability) {
                is UpdateAvailability.Pending -> showUpdatePrompt(
                    availability = availability,
                    source = UpdatePromptSource.MANUAL,
                )

                UpdateAvailability.NotAvailable ->
                    uiAction.emit(ProfileUiAction.ShowSnackbar(ProfileMessage.UP_TO_DATE))

                UpdateAvailability.CheckFailed ->
                    uiAction.emit(ProfileUiAction.ShowSnackbar(ProfileMessage.UPDATE_CHECK_FAILED))
            }
        }
    }

    private fun privacyOptionsClick() {
        launchExternalAction(ProfileOptionItemType.PRIVACY_OPTIONS) {
            val form = viewModelScope.launch { adsConsentService.showPrivacyOptions() }
            /*
             * Why: once up, the form covers the row; the timeout keeps a form that never reports back (its activity
             * recreated meanwhile) from leaving the row ignored until the screen is gone.
             */
            withTimeoutOrNull(privacyOptionsRepeatGuard) { form.join() }
        }
    }

    // Why: the back stack already drops a second confirmation, but a second "nothing to delete" message would show.
    private fun deleteProgressClick() {
        launchExternalAction(ProfileOptionItemType.DELETE_PROGRESS) {
            val progress = calculateBibleProgress().first()
            if (progress > 0) {
                navigator.navigate(DeleteAllProgressNavRoute)
            } else {
                uiAction.emit(ProfileUiAction.ShowNoProgressToDelete)
            }
        }
    }

    private fun navigateIfOnline(
        route: NavKey,
        offlineMessage: ProfileMessage,
    ) {
        viewModelScope.launch {
            if (isConnected()) {
                navigator.navigate(route)
            } else {
                uiAction.emit(ProfileUiAction.ShowSnackbar(offlineMessage))
            }
        }
    }

    private fun goToRoute(route: NavKey) {
        navigator.navigate(route)
    }

    /*
     * Why: the browser opens a moment after the tap, so a double tap would otherwise open the page twice; repeats
     * within a short window are ignored.
     */
    private fun openLink(
        type: ProfileOptionItemType,
        url: String,
    ) {
        launchExternalAction(type) {
            uiAction.emit(OpenLink(url))
            delay(linkRepeatGuard)
        }
    }

    /*
     * Why: these rows open something above the profile (a page, the consent form, a dialog) only after suspending,
     * so a second tap on the same row meanwhile is ignored instead of opening it twice.
     */
    private fun launchExternalAction(
        type: ProfileOptionItemType,
        action: suspend () -> Unit,
    ) {
        if (externalActionJobs[type]?.isActive == true) return
        externalActionJobs[type] = viewModelScope.launch { action() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
