package com.quare.bibleplanner.feature.donation.presentation

sealed interface DonationUiEvent {
    data object Dismiss : DonationUiEvent

    data class Copy(
        val type: DonationType,
    ) : DonationUiEvent

    data object OpenGitHubSponsors : DonationUiEvent

    data object ToggleBitcoin : DonationUiEvent

    data object ToggleUsdt : DonationUiEvent

    data object TogglePix : DonationUiEvent

    data object OpenPixQr : DonationUiEvent
}
