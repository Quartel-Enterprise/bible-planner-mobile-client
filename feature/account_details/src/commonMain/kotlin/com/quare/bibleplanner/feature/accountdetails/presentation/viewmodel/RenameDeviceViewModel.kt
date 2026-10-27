package com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_action_error
import com.quare.bibleplanner.core.devices.domain.usecase.RenameDevice
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiAction
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class RenameDeviceViewModel(
    private val renameDevice: RenameDevice,
    private val navigator: Navigator,
    route: RenameDeviceNavRoute,
    trackEvent: TrackEvent,
) : TrackedViewModel<RenameDeviceUiEvent>(trackEvent) {
    val currentName: String = route.currentName
    private val deviceRowId: String = route.deviceRowId

    val uiAction: SharedFlow<RenameDeviceUiAction>
        field = MutableSharedFlow<RenameDeviceUiAction>()

    override fun handleEvent(event: RenameDeviceUiEvent) {
        when (event) {
            is RenameDeviceUiEvent.OnConfirmClick -> confirmClick(event.newName)
            RenameDeviceUiEvent.OnDismiss -> navigator.navigateBack()
        }
    }

    private fun confirmClick(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed == currentName) {
            navigator.navigateBack()
            return
        }
        trackEvent(
            name = AnalyticsEventNames.DEVICE_RENAMED,
            params = emptyMap(),
        )
        viewModelScope.launch {
            // Why: going back clears this ViewModel and its scope, which would cancel the rename in flight.
            renameDevice(
                deviceRowId = deviceRowId,
                name = trimmed,
            ).onFailure {
                uiAction.emit(RenameDeviceUiAction.ShowSnackbar(Res.string.account_details_action_error))
            }
            navigator.navigateBack()
        }
    }
}
