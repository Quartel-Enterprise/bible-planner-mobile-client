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
        assertEquals(RelativeTime.JustNow, (now - 30.seconds).toRelativeTime(now))
    }

    @Test
    fun `GIVEN minutes ago WHEN mapping THEN MinutesAgo`() {
        assertEquals(RelativeTime.MinutesAgo(5), (now - 5.minutes).toRelativeTime(now))
    }

    @Test
    fun `GIVEN hours ago WHEN mapping THEN HoursAgo`() {
        assertEquals(RelativeTime.HoursAgo(3), (now - 3.hours).toRelativeTime(now))
    }

    @Test
    fun `GIVEN more than a day ago WHEN mapping THEN OlderThanADay`() {
        assertIs<RelativeTime.OlderThanADay>((now - 3.days).toRelativeTime(now))
    }
}
