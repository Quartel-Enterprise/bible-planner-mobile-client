package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import kotlin.time.Clock
import kotlin.time.Instant

internal class ChatConversationMapper {
    fun map(dto: ChatConversationDto): ChatConversationModel = ChatConversationModel(
        id = dto.id,
        title = dto.title.orEmpty(),
        preview = dto.preview,
        contextLabel = dto.context?.label,
        updatedAt = Instant.parse(dto.updatedAt),
    )

    fun map(
        conversationId: String,
        title: String,
        preview: String,
        contextLabel: String?,
    ): ChatConversationModel = ChatConversationModel(
        id = conversationId,
        title = title,
        preview = preview,
        contextLabel = contextLabel,
        updatedAt = Clock.System.now(),
    )
}
