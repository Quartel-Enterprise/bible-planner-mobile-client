package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class ChapterReadMapperTest {
    private val mapper = ChapterReadMapper()

    @Test
    fun `toDto maps entity fields and serializes timestamp as ISO-8601 round-trippable to epoch`() {
        val epochMillis = 1_749_715_200_123L
        val entity = ChapterEntity(
            id = 10,
            number = 3,
            bookId = "GEN",
            isRead = true,
            readUpdatedAt = epochMillis,
            isReadPendingSync = true,
        )

        val dto = mapper.toDto(userId = "user-1", entity = entity)

        assertEquals("user-1", dto.userId)
        assertEquals("GEN", dto.bookId)
        assertEquals(3, dto.chapterNumber)
        assertTrue(dto.isRead)
        assertEquals(epochMillis, mapper.toEpochMillis(dto.updatedAt))
    }

    @Test
    fun `toEpochMillis parses PostgREST timestamptz with microseconds and zero offset`() {
        val parsed = mapper.toEpochMillis("2026-06-12T08:00:00.123456+00:00")

        val expected = Instant.parse("2026-06-12T08:00:00.123Z").toEpochMilliseconds()
        assertEquals(expected, parsed)
    }
}
