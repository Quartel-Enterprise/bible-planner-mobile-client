package com.quare.bibleplanner.feature.deleteaccount.presentation.model

import org.jetbrains.compose.resources.StringResource

internal data class DeleteAccountUiState(
    val status: DeleteAccountUiStatus,
    val confirmationText: String,
    val isConfirmEnabled: Boolean,
    val isProUser: Boolean,
    val storeNameRes: StringResource?,
)
