package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlin.time.Instant

internal class ChatMessageMapper {
    fun map(dto: ChatMessageDto): ChatMessageModel = ChatMessageModel(
        id = dto.id,
        role = if (dto.role == ASSISTANT_ROLE) ChatRoleModel.ASSISTANT else ChatRoleModel.USER,
        content = dto.content,
        isStreaming = false,
        isFailed = dto.status == FAILED_STATUS,
        createdAt = Instant.parse(dto.createdAt),
    )

    private companion object {
        const val ASSISTANT_ROLE = "assistant"
        const val FAILED_STATUS = "failed"
    }
}
