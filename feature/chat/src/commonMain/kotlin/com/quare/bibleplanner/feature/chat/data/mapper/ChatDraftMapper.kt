package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import com.quare.bibleplanner.feature.chat.data.dto.ChatDraftDto
import kotlin.time.Instant

internal class ChatDraftMapper {
    fun toDto(
        userId: String,
        entity: ChatDraftEntity,
    ): ChatDraftDto = ChatDraftDto(
        userId = userId,
        threadKey = entity.threadKey,
        content = entity.content,
        updatedAt = Instant.fromEpochMilliseconds(entity.updatedAtEpochMillis).toString(),
    )

    fun toEpochMillis(updatedAt: String): Long = Instant.parse(updatedAt).toEpochMilliseconds()
}
