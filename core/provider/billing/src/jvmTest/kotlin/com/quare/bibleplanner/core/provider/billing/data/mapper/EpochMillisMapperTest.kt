package com.quare.bibleplanner.core.provider.billing.data.mapper

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class EpochMillisMapperTest {
    private lateinit var mapper: EpochMillisMapper

    @Test
    fun `should map an ISO date to epoch milliseconds`() {
        // When
        val millis = mapper.map("2026-01-06T22:23:11Z")

        // Then
        assertEquals(
            expected = 1_767_738_191_000L,
            actual = millis,
        )
    }

    @Test
    fun `should map an ISO date with an offset`() {
        // When
        val millis = mapper.map("2026-01-06T19:23:11-03:00")

        // Then
        assertEquals(
            expected = 1_767_738_191_000L,
            actual = millis,
        )
    }

    @Test
    fun `should return null for an unparseable date`() {
        // When
        val millis = mapper.map("not-a-date")

        // Then
        assertNull(millis)
    }

    @Test
    fun `should return null for a blank date`() {
        // When
        val millis = mapper.map("")

        // Then
        assertNull(millis)
    }

    @BeforeTest
    fun setUp() {
        mapper = EpochMillisMapper()
    }
}
