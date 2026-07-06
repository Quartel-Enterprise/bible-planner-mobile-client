package com.quare.bibleplanner.feature.daystudy.data.model

import com.quare.bibleplanner.feature.daystudy.data.dto.ChapterRequestDto
import com.quare.bibleplanner.feature.daystudy.data.dto.PassageRequestDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class DayStudyCacheKeyTest {
    @Test
    fun `GIVEN a single whole chapter WHEN asStorageKey THEN renders book colon number pipe version pipe language`() {
        // Given
        val key = cacheKey(passage("GENESIS", chapter(1)))

        // When
        val storageKey = key.asStorageKey()

        // Then
        assertEquals("GENESIS:1|ACF|pt-BR", storageKey)
    }

    @Test
    fun `GIVEN multiple chapters WHEN asStorageKey THEN they are comma joined`() {
        // Given
        val key = cacheKey(passage("GENESIS", chapter(1), chapter(2), chapter(3)))

        // When
        val storageKey = key.asStorageKey()

        // Then
        assertEquals("GENESIS:1,2,3|ACF|pt-BR", storageKey)
    }

    @Test
    fun `GIVEN a full verse range WHEN asStorageKey THEN it is rendered in brackets`() {
        // Given
        val key = cacheKey(
            passage("SECOND_SAMUEL", chapter(number = 5, startVerse = 1, endVerse = 10)),
        )

        // When
        val storageKey = key.asStorageKey()

        // Then
        assertEquals("SECOND_SAMUEL:5[1-10]|ACF|pt-BR", storageKey)
    }

    @Test
    fun `GIVEN only a start verse WHEN asStorageKey THEN the end side stays empty`() {
        // Given
        val key = cacheKey(passage("PSALMS", chapter(number = 119, startVerse = 89)))

        // When
        val storageKey = key.asStorageKey()

        // Then
        assertEquals("PSALMS:119[89-]|ACF|pt-BR", storageKey)
    }

    @Test
    fun `GIVEN only an end verse WHEN asStorageKey THEN the start side stays empty`() {
        // Given
        val key = cacheKey(passage("PSALMS", chapter(number = 119, endVerse = 88)))

        // When
        val storageKey = key.asStorageKey()

        // Then
        assertEquals("PSALMS:119[-88]|ACF|pt-BR", storageKey)
    }

    @Test
    fun `GIVEN passages and chapters out of order WHEN asStorageKey THEN output is normalized and stable`() {
        // Given
        val key = cacheKey(
            passage("PSALMS", chapter(122), chapter(32)),
            passage("GENESIS", chapter(1)),
        )

        // When
        val storageKey = key.asStorageKey()

        // Then
        assertEquals("GENESIS:1;PSALMS:32,122|ACF|pt-BR", storageKey)
    }

    @Test
    fun `GIVEN the same passages in a different order WHEN asStorageKey THEN both keys match`() {
        // Given
        val first = cacheKey(
            passage("PSALMS", chapter(122), chapter(32)),
            passage("GENESIS", chapter(1)),
        )
        val second = cacheKey(
            passage("GENESIS", chapter(1)),
            passage("PSALMS", chapter(32), chapter(122)),
        )

        // When & Then
        assertEquals(first.asStorageKey(), second.asStorageKey())
    }

    @Test
    fun `GIVEN different verse ranges of the same chapter WHEN asStorageKey THEN keys differ`() {
        // Given
        val firstHalf = cacheKey(
            passage("SECOND_SAMUEL", chapter(number = 5, startVerse = 1, endVerse = 10)),
        )
        val secondHalf = cacheKey(
            passage("SECOND_SAMUEL", chapter(number = 5, startVerse = 11, endVerse = 25)),
        )

        // When & Then
        assertNotEquals(firstHalf.asStorageKey(), secondHalf.asStorageKey())
    }

    @Test
    fun `GIVEN a different version or language WHEN asStorageKey THEN keys differ`() {
        // Given
        val base = cacheKey(passage("GENESIS", chapter(1)))

        // When & Then
        assertNotEquals(base.asStorageKey(), base.copy(version = "WEB").asStorageKey())
        assertNotEquals(base.asStorageKey(), base.copy(language = "en").asStorageKey())
    }

    private fun cacheKey(vararg passages: PassageRequestDto) = DayStudyCacheKey(
        passages = passages.toList(),
        version = "ACF",
        language = "pt-BR",
    )

    private fun passage(
        book: String,
        vararg chapters: ChapterRequestDto,
    ) = PassageRequestDto(
        book = book,
        chapters = chapters.toList(),
    )

    private fun chapter(
        number: Int,
        startVerse: Int? = null,
        endVerse: Int? = null,
    ) = ChapterRequestDto(
        number = number,
        startVerse = startVerse,
        endVerse = endVerse,
    )
}
