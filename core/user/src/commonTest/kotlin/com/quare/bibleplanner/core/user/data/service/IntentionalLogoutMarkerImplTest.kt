package com.quare.bibleplanner.core.user.data.service

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntentionalLogoutMarkerImplTest {
    private lateinit var marker: IntentionalLogoutMarkerImpl

    @BeforeTest
    fun setUp() {
        marker = IntentionalLogoutMarkerImpl()
    }

    @Test
    fun `GIVEN nothing marked WHEN consuming THEN reports no intentional logout`() {
        // When
        val consumed = marker.consume()

        // Then
        assertFalse(consumed)
    }

    @Test
    fun `GIVEN a marked logout WHEN consuming twice THEN reports it only once`() {
        // Given
        marker.mark()

        // When
        val first = marker.consume()
        val second = marker.consume()

        // Then
        assertTrue(first)
        assertFalse(second)
    }

    @Test
    fun `GIVEN a marked logout WHEN unmarking THEN there is nothing to consume`() {
        // Given
        marker.mark()

        // When
        marker.unmark()

        // Then
        assertFalse(marker.consume())
    }
}
