package com.quare.bibleplanner.feature.donation.presentation.factory

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.isPortugueseBrazil
import com.quare.bibleplanner.feature.donation.presentation.DonationSection
import com.quare.bibleplanner.feature.donation.presentation.DonationUiState

class DonationUiStateFactory(
    private val languageProvider: LanguageProvider,
) {
    fun create(): DonationUiState = DonationUiState(
        isBitcoinExpanded = false,
        isUsdtExpanded = false,
        isPixExpanded = false,
        copiedType = null,
        sections = buildSections(),
    )

    private fun buildSections(): List<DonationSection> {
        val isPortugueseBrazil = languageProvider.getDeviceLanguage().isPortugueseBrazil
        val ordered = if (isPortugueseBrazil) {
            listOf(DonationSection.PIX, DonationSection.BITCOIN, DonationSection.USDT)
        } else {
            listOf(DonationSection.BITCOIN, DonationSection.USDT, DonationSection.PIX)
        }
        return ordered + DonationSection.GITHUB
    }
}
