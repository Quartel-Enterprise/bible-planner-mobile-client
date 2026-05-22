package com.quare.bibleplanner.feature.donation.presentation

data class DonationUiState(
    val isBitcoinExpanded: Boolean,
    val isUsdtExpanded: Boolean,
    val isPixExpanded: Boolean,
    val copiedType: DonationType?,
    val sections: List<DonationSection>,
)
