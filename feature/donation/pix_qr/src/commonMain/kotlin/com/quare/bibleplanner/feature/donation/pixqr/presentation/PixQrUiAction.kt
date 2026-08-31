package com.quare.bibleplanner.feature.donation.pixqr.presentation

sealed interface PixQrUiAction {
    data class ShareQrCode(
        val message: String,
    ) : PixQrUiAction
}
