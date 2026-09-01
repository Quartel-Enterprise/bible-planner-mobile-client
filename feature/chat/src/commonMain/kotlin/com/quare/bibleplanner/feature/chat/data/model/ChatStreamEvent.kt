package com.quare.bibleplanner.feature.chat.data.model

import com.quare.bibleplanner.feature.chat.data.dto.ChatAcceptedDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatDoneDto

internal sealed interface ChatStreamEvent {
    data class Accepted(
        val payload: ChatAcceptedDto,
    ) : ChatStreamEvent

    data class Delta(
        val text: String,
    ) : ChatStreamEvent

    data object Restart : ChatStreamEvent

    data class Done(
        val payload: ChatDoneDto,
    ) : ChatStreamEvent

    data class Title(
        val conversationId: String,
        val title: String,
    ) : ChatStreamEvent
}
