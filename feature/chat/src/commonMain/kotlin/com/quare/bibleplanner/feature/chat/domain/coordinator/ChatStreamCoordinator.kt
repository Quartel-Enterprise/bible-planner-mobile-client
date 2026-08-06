package com.quare.bibleplanner.feature.chat.domain.coordinator

import com.quare.bibleplanner.feature.chat.domain.model.ChatSendModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Owns the in-flight question. It runs outside the screen's scope, so navigating away mid-answer
 * (or rotating the device) keeps the generation going and coming back re-attaches to it.
 */
interface ChatStreamCoordinator {
    val send: StateFlow<ChatSendModel?>

    fun start(request: ChatSendRequestModel)

    fun retry()

    fun clearFailure()
}
