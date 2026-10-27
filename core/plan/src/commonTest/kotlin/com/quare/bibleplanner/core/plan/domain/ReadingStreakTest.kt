package com.quare.bibleplanner.core.plan.domain

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.plan.fake.day
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReadingStreakTest {
    @Test
    fun `GIVEN an empty plan WHEN computing the streak THEN returns zero`() {
        // Given
        val days = emptyList<DayModel>()

        // When
        val streak = days.readingStreak

        // Then
        assertEquals(0, streak)
    }

    @Test
    fun `GIVEN the first day is unread WHEN computing the streak THEN returns zero even with later days read`() {
        // Given
        val days = daysWithReadStatus(false, true, true)

        // When
        val streak = days.readingStreak

        // Then
        assertEquals(0, streak)
    }

    @Test
    fun `GIVEN every day is read WHEN computing the streak THEN returns the total number of days`() {
        // Given
        val days = daysWithReadStatus(true, true, true, true)

        // When
        val streak = days.readingStreak

        // Then
        assertEquals(4, streak)
    }

    @Test
    fun `GIVEN read days up to a gap WHEN computing the streak THEN counts the days before the first unread one`() {
        // Given
        val days = daysWithReadStatus(true, true, true, false, false)

        // When
        val streak = days.readingStreak

        // Then
        assertEquals(3, streak)
    }

    @Test
    fun `GIVEN days read ahead past a gap WHEN computing the streak THEN does not count the days after the gap`() {
        // Given
        val days = daysWithReadStatus(true, true, false, true, true, true)

        // When
        val streak = days.readingStreak

        // Then
        assertEquals(2, streak)
    }

    @Test
    fun `GIVEN read days up to a gap and no dated today WHEN computing the shown streak THEN counts them`() {
        // Given
        val days = daysWithReadStatus(true, true, false, true)

        // When
        val streak = days.shownReadingStreak

        // Then
        assertEquals(2, streak)
    }

    @Test
    fun `GIVEN the first unread day is today WHEN computing the shown streak THEN counts the days before it`() {
        // Given
        val days = daysWithReadStatus(true, true, false, false).markToday(index = 2)

        // When
        val streak = days.shownReadingStreak

        // Then
        assertEquals(2, streak)
    }

    @Test
    fun `GIVEN the first unread day is before today WHEN computing the shown streak THEN returns zero`() {
        // Given
        val days = daysWithReadStatus(true, false, false, false).markToday(index = 3)

        // When
        val streak = days.shownReadingStreak

        // Then
        assertEquals(0, streak)
    }

    @Test
    fun `GIVEN every day is read WHEN computing the shown streak THEN returns zero`() {
        // Given
        val days = daysWithReadStatus(true, true, true)

        // When
        val streak = days.shownReadingStreak

        // Then
        assertEquals(0, streak)
    }

    private fun List<DayModel>.markToday(index: Int): List<DayModel> = mapIndexed { dayIndex, day ->
        day.copy(isToday = dayIndex == index)
    }

    private fun daysWithReadStatus(vararg isRead: Boolean): List<DayModel> = isRead.mapIndexed { index, read ->
        day(
            number = index + 1,
            passages = emptyList(),
            isRead = read,
        )
    }
}
