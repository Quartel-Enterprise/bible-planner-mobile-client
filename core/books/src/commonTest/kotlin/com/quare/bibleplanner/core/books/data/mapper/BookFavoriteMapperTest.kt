package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class BookFavoriteMapperTest {
    private val mapper = BookFavoriteMapper()

    @Test
    fun `GIVEN a favorite book entity WHEN mapping it to a dto THEN keeps its fields and an epoch round-trip`() {
        // Given
        val epochMillis = 1_749_715_200_123L
        val entity = BookEntity(
            id = "GEN",
            isFavorite = true,
            favoriteUpdatedAt = epochMillis,
            isFavoritePendingSync = true,
            isRead = false,
        )

        // When
        val dto = mapper.toDto(userId = "user-1", entity = entity)

        // Then
        assertEquals("user-1", dto.userId)
        assertEquals("GEN", dto.bookId)
        assertTrue(dto.isFavorite)
        assertEquals(epochMillis, mapper.toEpochMillis(dto.updatedAt))
    }

    @Test
    fun `GIVEN a PostgREST timestamptz with microseconds WHEN parsing it THEN returns its epoch millis`() {
        // Given
        val timestamp = "2026-06-12T08:00:00.123456+00:00"

        // When
        val parsed = mapper.toEpochMillis(timestamp)

        // Then
        // Microsecond precision is truncated to milliseconds on the way to epoch millis.
        val expected = Instant.parse("2026-06-12T08:00:00.123Z").toEpochMilliseconds()
        assertEquals(expected, parsed)
    }

    @Test
    fun `GIVEN the same instant in UTC and in a non-UTC offset WHEN parsing both THEN returns the same epoch millis`() {
        // Given
        val utcTimestamp = "2026-06-12T08:00:00+00:00"
        val plusTwoTimestamp = "2026-06-12T10:00:00+02:00"

        // When
        val utc = mapper.toEpochMillis(utcTimestamp)
        val plusTwo = mapper.toEpochMillis(plusTwoTimestamp)

        // Then
        assertEquals(utc, plusTwo)
    }
}
