package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ListeningFollowRequestsTest {
    private val requests = ListeningFollowRequests()
    private val genesisTwo = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 2)
    private val genesisThree = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 3)

    @Test
    fun `GIVEN a requested chapter WHEN consuming it twice THEN only the first one matches`() {
        // Given
        requests.request(genesisTwo)

        // When
        val consumed = listOf(requests.consume(genesisTwo), requests.consume(genesisTwo))

        // Then
        assertEquals(listOf(true, false), consumed)
    }

    @Test
    fun `GIVEN a request that never landed WHEN another reader opens THEN clears it`() {
        // Given
        requests.request(genesisTwo)

        // When
        val consumed = listOf(requests.consume(genesisThree), requests.consume(genesisTwo))

        // Then
        assertEquals(listOf(false, false), consumed)
    }
}
