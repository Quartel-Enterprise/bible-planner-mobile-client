package com.quare.bibleplanner.feature.donation.pixqr.presentation

sealed interface PixQrUiEvent {
    data object Dismiss : PixQrUiEvent

    data object Share : PixQrUiEvent
}
