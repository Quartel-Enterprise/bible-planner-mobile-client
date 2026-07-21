package com.quare.bibleplanner.feature.editprofile.presentation.model

import org.jetbrains.compose.resources.StringResource

internal sealed interface EditNameUiAction {
    data object NavigateBack : EditNameUiAction

    data class ShowSnackbar(
        val message: StringResource,
    ) : EditNameUiAction
}
