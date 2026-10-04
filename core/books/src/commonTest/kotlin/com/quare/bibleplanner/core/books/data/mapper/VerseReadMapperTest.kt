package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VerseReadMapperTest {
    private val mapper = VerseReadMapper()

    @Test
    fun `GIVEN a pending verse read WHEN mapping it to a dto THEN keeps its fields and an epoch round-trip`() {
        // Given
        val epochMillis = 1_749_715_200_123L
        val pending = PendingVerseRead(
            bookId = "PSA",
            chapterNumber = 119,
            verseNumber = 5,
            isRead = true,
            readUpdatedAt = epochMillis,
        )

        // When
        val dto = mapper.toDto(userId = "user-1", entity = pending)

        // Then
        assertEquals("user-1", dto.userId)
        assertEquals("PSA", dto.bookId)
        assertEquals(119, dto.chapterNumber)
        assertEquals(5, dto.verseNumber)
        assertTrue(dto.isRead)
        assertEquals(epochMillis, mapper.toEpochMillis(dto.updatedAt))
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
