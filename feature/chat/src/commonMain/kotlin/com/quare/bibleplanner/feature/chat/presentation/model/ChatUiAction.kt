package com.quare.bibleplanner.feature.chat.presentation.model

sealed interface ChatUiAction {
    data object ScrollToBottom : ChatUiAction
}
