package com.quare.bibleplanner.android.feature.paywallteaser

import androidx.annotation.StringRes

internal data class PaywallTeaserCopy(
    @StringRes val title: Int,
    @StringRes val body: Int,
    @StringRes val dismiss: Int,
)
