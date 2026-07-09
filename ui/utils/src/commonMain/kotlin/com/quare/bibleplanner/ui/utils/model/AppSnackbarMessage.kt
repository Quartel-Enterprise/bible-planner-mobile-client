package com.quare.bibleplanner.ui.utils.model

import org.jetbrains.compose.resources.StringResource

data class AppSnackbarMessage(
    val stringResource: StringResource,
    val isDismissible: Boolean,
)
