package com.quare.bibleplanner.feature.donation.presentation

import com.quare.bibleplanner.feature.donation.generated.DonationBuildKonfig

val DonationType.address: String
    get() = when (this) {
        DonationType.BTC_ONCHAIN -> DonationBuildKonfig.BTC_ONCHAIN
        DonationType.BTC_LIGHTNING -> DonationBuildKonfig.BTC_LIGHTNING
        DonationType.USDT_ERC20 -> DonationBuildKonfig.USDT_ERC20
        DonationType.USDT_TRC20 -> DonationBuildKonfig.USDT_TRC20
        DonationType.PIX -> DonationBuildKonfig.PIX_KEY
    }
