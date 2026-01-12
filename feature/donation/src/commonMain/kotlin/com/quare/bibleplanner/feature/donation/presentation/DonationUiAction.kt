package com.quare.bibleplanner.feature.donation.presentation

sealed interface DonationUiAction {
    data object Close : DonationUiAction

    data object NavigateBack : DonationUiAction

    data class Copy(
        val text: String,
    ) : DonationUiAction

    data class OpenUrl(
        val url: String,
    ) : DonationUiAction

    data object NavigateToPixQr : DonationUiAction
}
