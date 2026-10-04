package com.quare.bibleplanner.core.provider.billing.data.mapper

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class EpochMillisMapperTest {
    private lateinit var mapper: EpochMillisMapper

    @Test
    fun `GIVEN an ISO date WHEN mapping THEN returns its epoch milliseconds`() {
        // When
        val millis = mapper.map("2026-01-06T22:23:11Z")

        // Then
        assertEquals(
            expected = 1_767_738_191_000L,
            actual = millis,
        )
    }

    @Test
    fun `GIVEN an ISO date with an offset WHEN mapping THEN returns its epoch milliseconds`() {
        // When
        val millis = mapper.map("2026-01-06T19:23:11-03:00")

        // Then
        assertEquals(
            expected = 1_767_738_191_000L,
            actual = millis,
        )
    }

    @Test
    fun `GIVEN an unparseable date WHEN mapping THEN returns null`() {
        // When
        val millis = mapper.map("not-a-date")

        // Then
        assertNull(millis)
    }

    @Test
    fun `GIVEN a blank date WHEN mapping THEN returns null`() {
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
