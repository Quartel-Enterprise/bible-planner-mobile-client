package com.quare.bibleplanner.core.utils.version

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VersionComparatorTest {
    @Test
    fun `GIVEN a higher major WHEN comparing THEN returns positive`() {
        // Given
        val first = "2.0.0"
        val second = "1.9.9"

        // When
        val result = VersionComparator.compare(
            v1 = first,
            v2 = second,
        )

        // Then
        assertTrue(result > 0)
    }

    @Test
    fun `GIVEN equal versions WHEN comparing THEN returns zero`() {
        // Given
        val first = "1.2.3"
        val second = "1.2.3"

        // When
        val result = VersionComparator.compare(
            v1 = first,
            v2 = second,
        )

        // Then
        assertEquals(0, result)
    }

    @Test
    fun `GIVEN a lower patch WHEN comparing THEN returns negative`() {
        // Given
        val first = "1.2.3"
        val second = "1.2.4"

        // When
        val result = VersionComparator.compare(
            v1 = first,
            v2 = second,
        )

        // Then
        assertTrue(result < 0)
    }

    @Test
    fun `GIVEN missing components WHEN comparing THEN treats them as zero`() {
        // Given
        val withoutPatch = "1.2"
        val withZeroPatch = "1.2.0"
        val higherWithoutPatch = "1.3"
        val lowerWithPatch = "1.2.9"

        // When
        val equalResult = VersionComparator.compare(
            v1 = withoutPatch,
            v2 = withZeroPatch,
        )
        val higherResult = VersionComparator.compare(
            v1 = higherWithoutPatch,
            v2 = lowerWithPatch,
        )

        // Then
        assertEquals(0, equalResult)
        assertTrue(higherResult > 0)
    }

    @Test
    fun `GIVEN non numeric components WHEN comparing THEN treats them as zero`() {
        // Given
        val first = "1.2.0"
        val second = "1.2.x"

        // When
        val result = VersionComparator.compare(
            v1 = first,
            v2 = second,
        )

        // Then
        assertEquals(0, result)
    }
}
