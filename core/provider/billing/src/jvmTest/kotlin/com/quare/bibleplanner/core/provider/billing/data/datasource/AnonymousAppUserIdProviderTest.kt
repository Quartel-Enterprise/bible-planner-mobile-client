package com.quare.bibleplanner.core.provider.billing.data.datasource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnonymousAppUserIdProviderTest {
    @Test
    fun `GIVEN two providers WHEN reading the anonymous id THEN both get the same RevenueCat anonymous id`() {
        // Given
        val first = AnonymousAppUserIdProvider()
        val second = AnonymousAppUserIdProvider()

        // When
        val firstId = first()
        val secondId = second()

        // Then
        assertTrue(firstId.startsWith("\$RCAnonymousID:"))
        assertEquals(
            expected = firstId,
            actual = secondId,
        )
    }
}
