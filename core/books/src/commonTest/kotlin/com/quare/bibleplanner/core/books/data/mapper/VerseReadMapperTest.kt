package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VerseReadMapperTest {
    private val mapper = VerseReadMapper()

    @Test
    fun `toDto maps projection fields and serializes timestamp as ISO-8601 round-trippable to epoch`() {
        val epochMillis = 1_749_715_200_123L
        val pending = PendingVerseRead(
            bookId = "PSA",
            chapterNumber = 119,
            verseNumber = 5,
            isRead = true,
            readUpdatedAt = epochMillis,
        )

        val dto = mapper.toDto(userId = "user-1", entity = pending)

        assertEquals("user-1", dto.userId)
        assertEquals("PSA", dto.bookId)
        assertEquals(119, dto.chapterNumber)
        assertEquals(5, dto.verseNumber)
        assertTrue(dto.isRead)
        assertEquals(epochMillis, mapper.toEpochMillis(dto.updatedAt))
    }

    @Test
    fun `toEpochMillis honors non-UTC offsets`() {
        val utc = mapper.toEpochMillis("2026-06-12T08:00:00+00:00")
        val plusTwo = mapper.toEpochMillis("2026-06-12T10:00:00+02:00")

        assertEquals(utc, plusTwo)
    }
}
