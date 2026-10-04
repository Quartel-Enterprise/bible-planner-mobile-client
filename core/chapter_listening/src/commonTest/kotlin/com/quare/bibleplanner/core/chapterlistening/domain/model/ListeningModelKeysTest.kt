package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ListeningModelKeysTest {
    @Test
    fun `GIVEN the analytics keys WHEN reading them THEN are the lowercase names`() {
        // Given
        val modes = ListeningModeModel.entries

        // When
        val keys =
            modes.map(ListeningModeModel::key) + ListeningSleepTimerOption.entries.map(ListeningSleepTimerOption::key)

        // Then
        assertEquals(
            listOf("chapter", "day_reading", "off", "fifteen_minutes", "thirty_minutes", "end_of_chapter"),
            keys,
        )
    }
}
