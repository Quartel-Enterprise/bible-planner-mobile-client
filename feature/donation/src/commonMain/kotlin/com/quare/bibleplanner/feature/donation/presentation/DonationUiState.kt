package com.quare.bibleplanner.feature.donation.presentation

data class DonationUiState(
    val isBitcoinExpanded: Boolean = false,
    val isUsdtExpanded: Boolean = false,
    val isPixExpanded: Boolean = false,
    val copiedType: DonationType? = null,
)
