package com.quare.bibleplanner.core.date

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class RelativeTimeTest {
    private val now = Instant.parse("2026-07-11T12:00:00Z")

    @Test
    fun `GIVEN less than a minute WHEN mapping THEN JustNow`() {
        // Given
        val instant = now - 30.seconds

        // When
        val relativeTime = instant.toRelativeTime(now)

        // Then
        assertEquals(RelativeTime.JustNow, relativeTime)
    }

    @Test
    fun `GIVEN minutes ago WHEN mapping THEN MinutesAgo`() {
        // Given
        val instant = now - 5.minutes

        // When
        val relativeTime = instant.toRelativeTime(now)

        // Then
        assertEquals(RelativeTime.MinutesAgo(5), relativeTime)
    }

    @Test
    fun `GIVEN hours ago WHEN mapping THEN HoursAgo`() {
        // Given
        val instant = now - 3.hours

        // When
        val relativeTime = instant.toRelativeTime(now)

        // Then
        assertEquals(RelativeTime.HoursAgo(3), relativeTime)
    }

    @Test
    fun `GIVEN more than a day ago WHEN mapping THEN OlderThanADay`() {
        // Given
        val instant = now - 3.days

        // When
        val relativeTime = instant.toRelativeTime(now)

        // Then
        assertIs<RelativeTime.OlderThanADay>(relativeTime)
    }
}
