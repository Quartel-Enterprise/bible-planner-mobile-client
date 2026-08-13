package com.quare.bibleplanner.feature.chat.domain.coordinator

import com.quare.bibleplanner.feature.chat.domain.model.ChatSendModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import kotlinx.coroutines.flow.StateFlow

interface ChatStreamCoordinator {
    val send: StateFlow<ChatSendModel?>

    fun start(request: ChatSendRequestModel)

    fun retry()

    fun clearFailure()
}
