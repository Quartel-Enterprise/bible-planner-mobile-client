package com.quare.bibleplanner.feature.deleteaccount.presentation.model

import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore

data class DeleteAccountUiState(
    val status: DeleteAccountUiStatus,
    val confirmationText: String,
    val isConfirmEnabled: Boolean,
    val isProUser: Boolean,
    val subscriptionStore: PurchaseStore?,
)
