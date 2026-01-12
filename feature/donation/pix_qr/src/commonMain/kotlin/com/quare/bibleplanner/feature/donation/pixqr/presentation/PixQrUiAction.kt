package com.quare.bibleplanner.feature.donation.pixqr.presentation

sealed interface PixQrUiAction {
    data object NavigateBack : PixQrUiAction

    data class ShareQrCode(
        val message: String,
    ) : PixQrUiAction
}
