package com.quare.bibleplanner.feature.main.presentation.model

sealed interface MainScreenUiEvent {
    data class BottomNavItemClicked(
        val route: Any,
    ) : MainScreenUiEvent
}
