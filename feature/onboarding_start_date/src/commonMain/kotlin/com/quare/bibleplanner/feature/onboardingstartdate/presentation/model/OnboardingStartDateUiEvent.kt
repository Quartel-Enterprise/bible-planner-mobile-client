package com.quare.bibleplanner.feature.onboardingstartdate.presentation.model

internal sealed interface OnboardingStartDateUiEvent {
    data object OnDismiss : OnboardingStartDateUiEvent

    data object OnSetDateClick : OnboardingStartDateUiEvent

    data object OnStartNowClick : OnboardingStartDateUiEvent

    data object OnDontShowAgainClick : OnboardingStartDateUiEvent
}
