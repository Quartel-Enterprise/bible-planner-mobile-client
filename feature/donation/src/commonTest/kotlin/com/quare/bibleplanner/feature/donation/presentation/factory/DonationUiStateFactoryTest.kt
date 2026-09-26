package com.quare.bibleplanner.feature.donation.presentation.factory

import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.donation.presentation.DonationSection
import com.quare.bibleplanner.feature.donation.presentation.DonationUiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DonationUiStateFactoryTest {
    private lateinit var factory: DonationUiStateFactory

    @Test
    fun `GIVEN a device in brazilian portuguese WHEN creating the state THEN lists pix first`() {
        // Given
        prepareScenario(deviceLanguage = Language.PORTUGUESE_BRAZIL)

        // When
        val state = factory.create()

        // Then
        assertEquals(
            DonationUiState(
                isBitcoinExpanded = false,
                isUsdtExpanded = false,
                isPixExpanded = false,
                copiedType = null,
                sections = listOf(
                    DonationSection.PIX,
                    DonationSection.BITCOIN,
                    DonationSection.USDT,
                    DonationSection.GITHUB,
                ),
            ),
            state,
        )
    }

    @Test
    fun `GIVEN a device in another language WHEN creating the state THEN lists pix after the crypto options`() {
        // Given
        prepareScenario(deviceLanguage = Language.SPANISH)

        // When
        val state = factory.create()

        // Then
        assertEquals(
            listOf(
                DonationSection.BITCOIN,
                DonationSection.USDT,
                DonationSection.PIX,
                DonationSection.GITHUB,
            ),
            state.sections,
        )
    }

    private fun prepareScenario(deviceLanguage: Language) {
        factory = DonationUiStateFactory(FixedDeviceLanguageProvider(deviceLanguage))
    }
}

private class FixedDeviceLanguageProvider(
    private val deviceLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = deviceLanguage

    override fun getAppLanguage(): Language = error("unused")
}
