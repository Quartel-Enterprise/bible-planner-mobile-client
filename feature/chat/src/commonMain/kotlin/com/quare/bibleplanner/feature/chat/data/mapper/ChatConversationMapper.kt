package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.feature.chat.data.dto.ChatContextDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import kotlin.time.Clock
import kotlin.time.Instant

internal class ChatConversationMapper {
    fun map(dto: ChatConversationDto): ChatConversationModel = ChatConversationModel(
        id = dto.id,
        title = dto.title.orEmpty(),
        preview = dto.preview,
        contextLabel = dto.context?.label,
        planDay = dto.context?.planDay(),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    // Conversations created before the server started recording the plan day have none, and are
    // simply never matched to a day: the reader gets a new thread once, and that one carries it.
    private fun ChatContextDto.planDay(): ChatPlanDayModel? = ChatPlanDayModel(
        dayNumber = dayNumber ?: return null,
        weekNumber = weekNumber ?: return null,
        readingPlanType = readingPlanType ?: return null,
    )

    fun map(
        conversationId: String,
        title: String,
        preview: String,
        contextLabel: String?,
        planDay: ChatPlanDayModel?,
    ): ChatConversationModel = ChatConversationModel(
        id = conversationId,
        title = title,
        preview = preview,
        contextLabel = contextLabel,
        planDay = planDay,
        updatedAt = Clock.System.now(),
    )
}
