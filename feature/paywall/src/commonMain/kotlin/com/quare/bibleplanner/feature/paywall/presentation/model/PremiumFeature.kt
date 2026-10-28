package com.quare.bibleplanner.feature.paywall.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface PremiumFeature {
    val title: StringResource
}
