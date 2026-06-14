package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class BookFavoriteMapperTest {
    private val mapper = BookFavoriteMapper()

    @Test
    fun `toDto maps entity fields and serializes timestamp as ISO-8601 round-trippable to epoch`() {
        val epochMillis = 1_749_715_200_123L
        val entity = BookEntity(
            id = "GEN",
            isFavorite = true,
            favoriteUpdatedAt = epochMillis,
            isFavoritePendingSync = true,
        )

        val dto = mapper.toDto(userId = "user-1", entity = entity)

        assertEquals("user-1", dto.userId)
        assertEquals("GEN", dto.bookId)
        assertTrue(dto.isFavorite)
        assertEquals(epochMillis, mapper.toEpochMillis(dto.updatedAt))
    }

    @Test
    fun `toEpochMillis parses PostgREST timestamptz with microseconds and zero offset`() {
        val parsed = mapper.toEpochMillis("2026-06-12T08:00:00.123456+00:00")

        // Microsecond precision is truncated to milliseconds on the way to epoch millis.
        val expected = Instant.parse("2026-06-12T08:00:00.123Z").toEpochMilliseconds()
        assertEquals(expected, parsed)
    }

    @Test
    fun `toEpochMillis honors non-UTC offsets`() {
        val utc = mapper.toEpochMillis("2026-06-12T08:00:00+00:00")
        val plusTwo = mapper.toEpochMillis("2026-06-12T10:00:00+02:00")

        assertEquals(utc, plusTwo)
    }
}
