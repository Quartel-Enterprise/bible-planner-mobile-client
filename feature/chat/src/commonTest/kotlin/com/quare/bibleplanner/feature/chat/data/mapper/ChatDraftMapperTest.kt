package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.ChatDraftEntity
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ChatDraftMapperTest {
    private val mapper = ChatDraftMapper()

    @Test
    fun `GIVEN a local draft WHEN mapping to the wire THEN the timestamp round-trips`() {
        val entity = ChatDraftEntity(
            threadKey = "day:CHRONOLOGICAL:1:2",
            content = "Por que",
            updatedAtEpochMillis = 1_784_000_000_000,
            isPendingSync = true,
        )

        val dto = mapper.toDto(
            userId = "user-1",
            entity = entity,
        )

        assertEquals("user-1", dto.userId)
        assertEquals("day:CHRONOLOGICAL:1:2", dto.threadKey)
        assertEquals("Por que", dto.content)
        assertEquals(1_784_000_000_000, mapper.toEpochMillis(dto.updatedAt))
    }
}
