package com.quare.bibleplanner.tools.agentcli.fake

internal sealed interface HomeUiEvent {
    data class OnDayClick(
        val dayNumber: Int,
    ) : HomeUiEvent

    data object OnSnackbarClick : HomeUiEvent

    data object OnSlowSaveClick : HomeUiEvent
}
