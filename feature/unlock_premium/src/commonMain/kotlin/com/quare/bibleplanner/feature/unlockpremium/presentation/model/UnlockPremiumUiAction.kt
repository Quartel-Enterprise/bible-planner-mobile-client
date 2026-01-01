package com.quare.bibleplanner.feature.unlockpremium.presentation.model

internal sealed interface UnlockPremiumUiAction {
    data object NavigateBack : UnlockPremiumUiAction
}
