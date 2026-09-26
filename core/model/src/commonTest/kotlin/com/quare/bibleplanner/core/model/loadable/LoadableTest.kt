package com.quare.bibleplanner.core.model.loadable

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LoadableTest {
    @Test
    fun `GIVEN a loaded value WHEN reading it THEN returns the value`() {
        // Given
        val loadable: Loadable<String> = Loadable.Loaded("verse")

        // When
        val value = loadable.valueOrNull()

        // Then
        assertEquals("verse", value)
    }

    @Test
    fun `GIVEN a loading state WHEN reading it THEN returns null`() {
        // Given
        val loadable: Loadable<String> = Loadable.Loading

        // When
        val value = loadable.valueOrNull()

        // Then
        assertNull(value)
    }
}
