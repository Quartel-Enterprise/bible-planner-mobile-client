package com.quare.bibleplanner.feature.chat.presentation.mapper

import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import com.quare.bibleplanner.feature.chat.presentation.model.ChatMessageUiModel

class ChatMessageUiMapper {
    // An assistant message with nothing written yet is the "thinking" state, not an empty bubble.
    // Ids are deduplicated here because the list feeds a lazy list, which throws on a repeated key:
    // dropping a duplicate is a far better outcome than taking the chat down mid-answer.
    fun map(messages: List<ChatMessageModel>): List<ChatMessageUiModel> = messages
        .distinctBy(ChatMessageModel::id)
        .filterNot { message -> message.isStreaming && message.content.isEmpty() }
        .map { message ->
            ChatMessageUiModel(
                id = message.id,
                text = message.content,
                isFromUser = message.role == ChatRoleModel.USER,
                isStreaming = message.isStreaming,
                isFailed = message.isFailed,
            )
        }

    fun isThinking(messages: List<ChatMessageModel>): Boolean = messages.any { message ->
        message.isStreaming && message.content.isEmpty()
    }
}
