package com.quare.bibleplanner.feature.chat.presentation.model

import androidx.navigation3.runtime.NavKey

sealed interface ChatUiAction {
    data class NavigateToRoute(
        val route: NavKey,
    ) : ChatUiAction

    data object ScrollToBottom : ChatUiAction
}
