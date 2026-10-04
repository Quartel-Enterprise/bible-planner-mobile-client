package com.quare.bibleplanner.tools.agentcli.fake

internal sealed interface DayScreenUiEvent {
    data object OnBackClick : DayScreenUiEvent

    data object OnCount : DayScreenUiEvent

    data class OnWidthClassChanged(
        val isWide: Boolean,
    ) : DayScreenUiEvent
}
