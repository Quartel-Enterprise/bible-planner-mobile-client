package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ChatConversationMapperTest {
    private val mapper = ChatConversationMapper()

    @Test
    fun `GIVEN a context carrying its plan day WHEN mapping THEN the day is read`() {
        val model = mapper.map(dto(context = context(withPlanDay = true)))

        assertEquals("Gênesis 4-7", model.contextLabel)
        assertEquals(
            ChatPlanDayModel(
                dayNumber = 2,
                weekNumber = 1,
                readingPlanType = "CHRONOLOGICAL",
            ),
            model.planDay,
        )
    }

    @Test
    fun `GIVEN a context frozen before the plan day existed WHEN mapping THEN the day is null`() {
        val model = mapper.map(dto(context = context(withPlanDay = false)))

        assertEquals("Gênesis 4-7", model.contextLabel)
        assertNull(model.planDay)
    }

    @Test
    fun `GIVEN a half-given plan day WHEN mapping THEN it is treated as none`() {
        val partial = buildJsonObject {
            put("label", "Gênesis 4-7")
            put("day_number", 2)
        }

        assertNull(mapper.map(dto(context = partial)).planDay)
    }

    @Test
    fun `GIVEN a context-free conversation WHEN mapping THEN label and day are null`() {
        val model = mapper.map(dto(context = null))

        assertNull(model.contextLabel)
        assertNull(model.planDay)
    }

    @Test
    fun `GIVEN a null title WHEN mapping THEN it becomes empty instead of failing`() {
        assertEquals(
            "",
            mapper
                .map(
                    dto(
                        context = null,
                        title = null,
                    ),
                ).title,
        )
    }

    private fun context(withPlanDay: Boolean): JsonObject = buildJsonObject {
        put("label", "Gênesis 4-7")
        put("version", "ACF")
        if (withPlanDay) {
            put("day_number", 2)
            put("week_number", 1)
            put("reading_plan_type", "CHRONOLOGICAL")
        }
    }

    private fun dto(
        context: JsonObject?,
        title: String? = "Caim e Abel",
    ): ChatConversationDto = ChatConversationDto(
        id = "conversation-1",
        title = title,
        preview = "Por que Caim matou Abel?",
        contextType = context?.let { "day_reading" },
        context = context,
        updatedAt = "2026-08-14T12:00:00Z",
    )
}
