package com.quare.bibleplanner.core.utils.version

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VersionComparatorTest {
    @Test
    fun `GIVEN a higher major WHEN comparing THEN returns positive`() {
        assertTrue(VersionComparator.compare("2.0.0", "1.9.9") > 0)
    }

    @Test
    fun `GIVEN equal versions WHEN comparing THEN returns zero`() {
        assertEquals(0, VersionComparator.compare("1.2.3", "1.2.3"))
    }

    @Test
    fun `GIVEN a lower patch WHEN comparing THEN returns negative`() {
        assertTrue(VersionComparator.compare("1.2.3", "1.2.4") < 0)
    }

    @Test
    fun `GIVEN missing components WHEN comparing THEN treats them as zero`() {
        assertEquals(0, VersionComparator.compare("1.2", "1.2.0"))
        assertTrue(VersionComparator.compare("1.3", "1.2.9") > 0)
    }

    @Test
    fun `GIVEN non numeric components WHEN comparing THEN treats them as zero`() {
        assertEquals(0, VersionComparator.compare("1.2.0", "1.2.x"))
    }
}
