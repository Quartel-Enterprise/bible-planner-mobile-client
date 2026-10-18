package com.quare.bibleplanner.feature.accountdetails.presentation.model

import software.quare.loadable.Loadable

internal data class AccountDetailsUiState(
    val accountInfo: Loadable<AccountInfo?>,
    val devices: Loadable<List<DeviceUiModel>>,
    val isDevicesExpanded: Boolean,
)
