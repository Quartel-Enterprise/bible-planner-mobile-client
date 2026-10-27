package com.quare.bibleplanner.feature.accountdetails.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface RenameDeviceUiAction {
    data class ShowSnackbar(
        val message: StringResource,
    ) : RenameDeviceUiAction
}
