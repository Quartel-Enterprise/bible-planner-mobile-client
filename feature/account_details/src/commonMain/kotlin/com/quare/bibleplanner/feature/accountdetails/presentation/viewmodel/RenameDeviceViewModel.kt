package com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.devices.domain.usecase.RenameDevice
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class RenameDeviceViewModel(
    private val renameDevice: RenameDevice,
    route: RenameDeviceNavRoute,
    trackEvent: TrackEvent,
) : TrackedViewModel<RenameDeviceUiEvent>(trackEvent) {
    val currentName: String = route.currentName
    private val deviceRowId: String = route.deviceRowId

    val backUiAction: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>()

    override fun handleEvent(event: RenameDeviceUiEvent) {
        when (event) {
            is RenameDeviceUiEvent.OnConfirmClick -> confirmClick(event.newName)
            RenameDeviceUiEvent.OnDismiss -> dismiss()
        }
    }

    private fun confirmClick(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty() && trimmed != currentName) {
            trackEvent(AnalyticsEventNames.DEVICE_RENAMED, emptyMap())
            viewModelScope.launch { renameDevice(deviceRowId, trimmed) }
        }
        navigateBack()
    }

    private fun dismiss() {
        navigateBack()
    }

    private fun navigateBack() {
        viewModelScope.launch { backUiAction.emit(Unit) }
    }
}
