package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock
import kotlin.time.Instant

internal class ChatConversationMapper {
    fun map(dto: ChatConversationDto): ChatConversationModel = ChatConversationModel(
        id = dto.id,
        title = dto.title.orEmpty(),
        preview = dto.preview,
        contextLabel = dto.context?.text(LABEL_KEY),
        planDay = dto.context?.planDay(),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    // Conversations created before the server started recording the plan day have none, and are
    // simply never matched to a day: the reader gets a new thread once, and that one carries it.
    private fun JsonObject.planDay(): ChatPlanDayModel? = ChatPlanDayModel(
        dayNumber = number(DAY_NUMBER_KEY) ?: return null,
        weekNumber = number(WEEK_NUMBER_KEY) ?: return null,
        readingPlanType = text(READING_PLAN_TYPE_KEY) ?: return null,
    )

    private fun JsonObject.text(key: String): String? = get(key)?.jsonPrimitive?.contentOrNull

    private fun JsonObject.number(key: String): Int? = get(key)?.jsonPrimitive?.intOrNull

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

    private companion object {
        const val LABEL_KEY = "label"
        const val DAY_NUMBER_KEY = "day_number"
        const val WEEK_NUMBER_KEY = "week_number"
        const val READING_PLAN_TYPE_KEY = "reading_plan_type"
    }
}
