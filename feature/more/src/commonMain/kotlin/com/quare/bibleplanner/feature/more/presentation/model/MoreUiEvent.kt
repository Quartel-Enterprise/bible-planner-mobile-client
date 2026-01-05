package com.quare.bibleplanner.feature.more.presentation.model

internal sealed interface MoreUiEvent {
    data class OnItemClick(val type: MoreOptionItemType) : MoreUiEvent
}
