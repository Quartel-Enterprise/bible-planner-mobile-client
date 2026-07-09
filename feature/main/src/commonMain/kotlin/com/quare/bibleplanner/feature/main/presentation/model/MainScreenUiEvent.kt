package com.quare.bibleplanner.feature.main.presentation.model

import androidx.navigation3.runtime.NavKey

sealed interface MainScreenUiEvent {
    data class BottomNavItemClicked(
        val route: NavKey,
    ) : MainScreenUiEvent
}
