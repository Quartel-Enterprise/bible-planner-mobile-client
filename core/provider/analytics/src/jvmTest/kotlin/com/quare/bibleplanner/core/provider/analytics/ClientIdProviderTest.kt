package com.quare.bibleplanner.core.provider.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClientIdProviderTest {
    @Test
    fun `GIVEN two providers WHEN reading the client id THEN both get the same persisted id`() {
        // Given
        val first = ClientIdProvider()
        val second = ClientIdProvider()

        // When
        val firstId = first.getClientId()
        val secondId = second.getClientId()

        // Then
        assertTrue(firstId.isNotBlank())
        assertEquals(
            expected = firstId,
            actual = secondId,
        )
    }
}
