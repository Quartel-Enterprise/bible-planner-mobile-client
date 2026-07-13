package com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_action_error
import bibleplanner.feature.account_details.generated.resources.account_details_device_signed_out
import com.quare.bibleplanner.core.devices.domain.model.DeviceModel
import com.quare.bibleplanner.core.devices.domain.usecase.ObserveDevices
import com.quare.bibleplanner.core.devices.domain.usecase.SignOutDevice
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.model.UserModel
import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUser
import com.quare.bibleplanner.feature.accountdetails.presentation.mapper.DeviceUiModelMapper
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiAction
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountDetailsUiState
import com.quare.bibleplanner.feature.accountdetails.presentation.model.AccountInfo
import com.quare.bibleplanner.feature.accountdetails.presentation.model.DeviceUiModel
import com.quare.bibleplanner.feature.accountdetails.presentation.model.LoginMethod
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class AccountDetailsViewModel(
    observeCurrentUser: ObserveCurrentUser,
    observeDevices: ObserveDevices,
    private val deviceUiModelMapper: DeviceUiModelMapper,
    private val signOutDevice: SignOutDevice,
    trackEvent: TrackEvent,
) : TrackedViewModel<AccountDetailsUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<AccountDetailsUiAction>()
    val uiAction: SharedFlow<AccountDetailsUiAction> = _uiAction

    private val isDevicesExpanded = MutableStateFlow(false)
    private val signingOutDeviceIds = MutableStateFlow<Set<String>>(emptySet())

    private val accountInfoFlow: Flow<Loadable<AccountInfo?>> = observeCurrentUser()
        .map<UserModel?, Loadable<AccountInfo?>> { user -> Loadable.Loaded(user?.toAccountInfo()) }
        .onStart { emit(Loadable.Loading) }

    private val devicesFlow: Flow<Loadable<List<DeviceUiModel>>> = observeDevices()
        .map<List<DeviceModel>, Loadable<List<DeviceUiModel>>> { devices ->
            Loadable.Loaded(devices.map(deviceUiModelMapper::map))
        }.onStart { emit(Loadable.Loading) }

    val uiState: StateFlow<AccountDetailsUiState> = combine(
        accountInfoFlow,
        devicesFlow,
        isDevicesExpanded,
        signingOutDeviceIds,
    ) { accountInfo, devices, expanded, signingOutIds ->
        AccountDetailsUiState(
            accountInfo = accountInfo,
            devices = devices.withSigningOut(signingOutIds),
            isDevicesExpanded = expanded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = initialState(),
    )

    override fun handleEvent(event: AccountDetailsUiEvent) {
        when (event) {
            AccountDetailsUiEvent.OnToggleDevices -> toggleDevices()

            is AccountDetailsUiEvent.OnRenameDeviceClick -> emitAction(
                AccountDetailsUiAction.NavigateToRoute(
                    RenameDeviceNavRoute(deviceRowId = event.device.id, currentName = event.device.name),
                ),
            )

            is AccountDetailsUiEvent.OnSignOutDeviceClick -> signOutDeviceClick(event.device)

            AccountDetailsUiEvent.OnLogoutClick -> emitAction(AccountDetailsUiAction.ReplaceWithRoute(LogoutNavRoute))
        }
    }

    private fun toggleDevices() {
        val isExpanded = !isDevicesExpanded.value
        isDevicesExpanded.value = isExpanded
        trackEvent(
            AnalyticsEventNames.CONNECTED_DEVICES_TOGGLED,
            mapOf(AnalyticsParams.IS_EXPANDED to isExpanded),
        )
    }

    private fun signOutDeviceClick(device: DeviceUiModel) {
        if (device.isCurrentDevice) {
            emitAction(AccountDetailsUiAction.ReplaceWithRoute(LogoutNavRoute))
            return
        }
        if (device.id in signingOutDeviceIds.value) return
        trackEvent(AnalyticsEventNames.DEVICE_SIGNED_OUT, emptyMap())
        signingOutDeviceIds.update { ids -> ids + device.id }
        viewModelScope.launch {
            signOutDevice(device.id).fold(
                onSuccess = {
                    emitAction(AccountDetailsUiAction.ShowSnackbar(Res.string.account_details_device_signed_out))
                },
                onFailure = {
                    signingOutDeviceIds.update { ids -> ids - device.id }
                    emitAction(AccountDetailsUiAction.ShowSnackbar(Res.string.account_details_action_error))
                },
            )
        }
    }

    private fun Loadable<List<DeviceUiModel>>.withSigningOut(
        signingOutIds: Set<String>,
    ): Loadable<List<DeviceUiModel>> = when (this) {
        Loadable.Loading -> this
        is Loadable.Loaded -> Loadable.Loaded(value.map { it.copy(isSigningOut = it.id in signingOutIds) })
    }

    private fun emitAction(action: AccountDetailsUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }

    private fun initialState(): AccountDetailsUiState = AccountDetailsUiState(
        accountInfo = Loadable.Loading,
        devices = Loadable.Loading,
        isDevicesExpanded = false,
    )

    private fun UserModel.toAccountInfo(): AccountInfo = AccountInfo(
        loginMethod = provider.toLoginMethod(),
        lastSignInAt = lastSignInAt,
        createdAt = createdAt,
    )

    private fun String?.toLoginMethod(): LoginMethod = when (this?.lowercase()) {
        PROVIDER_GOOGLE -> LoginMethod.GOOGLE
        PROVIDER_APPLE -> LoginMethod.APPLE
        else -> LoginMethod.OTHER
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val PROVIDER_GOOGLE = "google"
        const val PROVIDER_APPLE = "apple"
    }
}
