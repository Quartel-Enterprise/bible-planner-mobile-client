package com.quare.bibleplanner.feature.more.presentation.model

internal sealed interface MoreUiEvent {
    data class OnItemClick(
        val type: MoreOptionItemType,
    ) : MoreUiEvent

    data object OnProCardClick : MoreUiEvent

    data object OnDismissSubscriptionDetailsDialog : MoreUiEvent

    data object OnDismissContactSupportDialog : MoreUiEvent

    data object OnSendSupportEmailClick : MoreUiEvent

    data object OnCopySupportEmailClick : MoreUiEvent

    data object OnLoginClick : MoreUiEvent

    data object OnLogoutClick : MoreUiEvent
}
