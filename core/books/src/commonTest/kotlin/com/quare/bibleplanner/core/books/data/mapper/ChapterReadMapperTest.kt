package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class ChapterReadMapperTest {
    private val mapper = ChapterReadMapper()

    @Test
    fun `GIVEN a read chapter entity WHEN mapping it to a dto THEN keeps its fields and an epoch round-trip`() {
        // Given
        val epochMillis = 1_749_715_200_123L
        val entity = ChapterEntity(
            id = 10,
            number = 3,
            bookId = "GEN",
            isRead = true,
            readUpdatedAt = epochMillis,
            isReadPendingSync = true,
        )

        // When
        val dto = mapper.toDto(userId = "user-1", entity = entity)

        // Then
        assertEquals("user-1", dto.userId)
        assertEquals("GEN", dto.bookId)
        assertEquals(3, dto.chapterNumber)
        assertTrue(dto.isRead)
        assertEquals(epochMillis, mapper.toEpochMillis(dto.updatedAt))
    }

    @Test
    fun `GIVEN a PostgREST timestamptz with microseconds WHEN parsing it THEN returns its epoch millis`() {
        // Given
        val timestamp = "2026-06-12T08:00:00.123456+00:00"

        // When
        val parsed = mapper.toEpochMillis(timestamp)

        // Then
        val expected = Instant.parse("2026-06-12T08:00:00.123Z").toEpochMilliseconds()
        assertEquals(expected, parsed)
    }
}
