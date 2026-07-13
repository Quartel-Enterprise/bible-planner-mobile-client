package com.quare.bibleplanner.feature.more.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.login_requires_internet
import bibleplanner.feature.more.generated.resources.logout_requires_internet
import bibleplanner.feature.more.generated.resources.up_to_date_message
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.model.legal.LegalUrl
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
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
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestInAppReview
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.web.GetWebAppUrl
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.more.domain.usecase.GetInstagramUrlUseCase
import com.quare.bibleplanner.feature.more.presentation.factory.MoreUiStateFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction.OpenLink
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
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

internal class MoreViewModel(
    uiStateFactory: MoreUiStateFactory,
    private val calculateBibleProgress: CalculateBibleProgressUseCase,
    private val getWebAppUrl: GetWebAppUrl,
    private val getInstagramUrl: GetInstagramUrlUseCase,
    private val getAppStoreLink: GetAppStoreLinkUseCase,
    private val requestInAppReview: RequestInAppReview,
    private val isConnected: IsConnected,
    private val checkForUpdate: CheckForUpdate,
    trackEvent: TrackEvent,
) : TrackedViewModel<MoreUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<MoreUiAction>()
    val uiAction: SharedFlow<MoreUiAction> = _uiAction
    private val isCheckingForUpdate = MutableStateFlow(false)
    val uiState: StateFlow<MoreUiState> = combine(
        uiStateFactory.create(),
        isCheckingForUpdate,
    ) { state, checking ->
        state.copy(isCheckingForUpdate = checking)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = uiStateFactory.initialState(),
    )

    override fun handleEvent(event: MoreUiEvent) {
        when (event) {
            is MoreUiEvent.OnItemClick -> {
                when (event.type) {
                    MoreOptionItemType.THEME -> goToRoute(ThemeNavRoute)

                    MoreOptionItemType.APP_LANGUAGE -> goToRoute(AppLanguageNavRoute)

                    MoreOptionItemType.PRIVACY_POLICY -> emitAction(OpenLink(LegalUrl.PRIVACY_POLICY))

                    MoreOptionItemType.TERMS -> emitAction(OpenLink(LegalUrl.TERMS_OF_SERVICE))

                    MoreOptionItemType.BECOME_PRO -> {
                        trackEvent(
                            name = AnalyticsEventNames.PAYWALL_VIEWED,
                            params = mapOf(AnalyticsParams.SOURCE to PAYWALL_SOURCE),
                        )
                        goToRoute(PaywallNavRoute)
                    }

                    MoreOptionItemType.INSTAGRAM -> emitAction(OpenLink(getInstagramUrl()))

                    MoreOptionItemType.EDIT_PLAN_START_DAY -> goToRoute(EditPlanStartDateNavRoute)

                    MoreOptionItemType.DELETE_PROGRESS -> deleteProgressClick()

                    MoreOptionItemType.DONATE -> goToRoute(DonationNavRoute)

                    MoreOptionItemType.WEB_APP -> {
                        viewModelScope.launch {
                            emitAction(OpenLink(getWebAppUrl()))
                        }
                    }

                    MoreOptionItemType.RELEASE_NOTES -> goToRoute(ReleaseNotesNavRoute)

                    MoreOptionItemType.BIBLE_VERSION -> goToRoute(BibleVersionSelectorRoute)

                    MoreOptionItemType.CONTACT_SUPPORT -> goToRoute(ContactSupportNavRoute)

                    MoreOptionItemType.RATE_APP -> rateAppClick()

                    MoreOptionItemType.CHECK_FOR_UPDATE -> checkForUpdateClick()
                }
            }

            MoreUiEvent.OnProCardClick -> goToRoute(SubscriptionDetailsNavRoute)

            MoreUiEvent.OnAccountCardClick -> goToRoute(AccountDetailsNavRoute)

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

    private fun rateAppClick() {
        viewModelScope.launch {
            val reviewLaunched = requestInAppReview()
            trackEvent(
                name = AnalyticsEventNames.RATE_APP_REVIEW_REQUESTED,
                params = mapOf(
                    AnalyticsParams.METHOD to if (reviewLaunched) METHOD_IN_APP_REVIEW else METHOD_STORE_REDIRECT,
                ),
            )
            if (!reviewLaunched) {
                emitAction(OpenLink(getAppStoreLink()))
            }
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
                    MoreUiAction.GoToRoute(
                        InAppUpdateNavRoute(
                            versionName = availability.versionName,
                            source = UpdatePromptSource.MANUAL,
                        ),
                    )

                UpdateAvailability.NotAvailable ->
                    MoreUiAction.ShowSnackbar(Res.string.up_to_date_message)
            }
            _uiAction.emit(action)
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
        route: NavKey,
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

    private fun goToRoute(route: NavKey) {
        emitAction(MoreUiAction.GoToRoute(route))
    }

    private fun emitAction(action: MoreUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    private companion object {
        const val PAYWALL_SOURCE = "more_menu"
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val METHOD_IN_APP_REVIEW = "in_app_review"
        const val METHOD_STORE_REDIRECT = "store_redirect"
    }
}
