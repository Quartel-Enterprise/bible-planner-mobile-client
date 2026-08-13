package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.ChatConversationEntity
import com.quare.bibleplanner.core.provider.room.entity.ChatMessageEntity
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlin.time.Instant

internal class ChatEntityMapper {
    fun mapConversation(entity: ChatConversationEntity): ChatConversationModel = ChatConversationModel(
        id = entity.id,
        title = entity.title,
        preview = entity.preview,
        contextLabel = entity.contextLabel,
        planDay = entity.planDay(),
        updatedAt = Instant.fromEpochMilliseconds(entity.updatedAtEpochMillis),
    )

    private fun ChatConversationEntity.planDay(): ChatPlanDayModel? = ChatPlanDayModel(
        dayNumber = dayNumber ?: return null,
        weekNumber = weekNumber ?: return null,
        readingPlanType = readingPlanType ?: return null,
    )

    fun mapConversation(model: ChatConversationModel): ChatConversationEntity = ChatConversationEntity(
        id = model.id,
        title = model.title,
        preview = model.preview,
        contextLabel = model.contextLabel,
        dayNumber = model.planDay?.dayNumber,
        weekNumber = model.planDay?.weekNumber,
        readingPlanType = model.planDay?.readingPlanType,
        updatedAtEpochMillis = model.updatedAt.toEpochMilliseconds(),
    )

    fun mapMessage(entity: ChatMessageEntity): ChatMessageModel = ChatMessageModel(
        id = entity.id,
        role = if (entity.isFromUser) ChatRoleModel.USER else ChatRoleModel.ASSISTANT,
        content = entity.content,
        isStreaming = false,
        isFailed = entity.isFailed,
        createdAt = Instant.fromEpochMilliseconds(entity.createdAtEpochMillis),
    )

    fun mapMessage(
        conversationId: String,
        model: ChatMessageModel,
    ): ChatMessageEntity = ChatMessageEntity(
        id = model.id,
        conversationId = conversationId,
        isFromUser = model.role == ChatRoleModel.USER,
        content = model.content,
        isFailed = model.isFailed,
        createdAtEpochMillis = model.createdAt.toEpochMilliseconds(),
    )
}
