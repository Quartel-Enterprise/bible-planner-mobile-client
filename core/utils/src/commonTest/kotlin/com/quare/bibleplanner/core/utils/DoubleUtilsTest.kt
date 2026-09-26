package com.quare.bibleplanner.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DoubleUtilsTest {
    @Test
    fun `GIVEN whole amounts WHEN formatting as money THEN drops the decimals`() {
        // When
        val labels = listOf(1.0, 10.0, 9.999).map(Double::toMoneyFormat)

        // Then
        assertEquals(listOf("1", "10", "10"), labels)
    }

    @Test
    fun `GIVEN fractional amounts WHEN formatting as money THEN shows two decimals`() {
        // When
        val labels = listOf(1.5, 10.99, 0.07, 2.345).map(Double::toMoneyFormat)

        // Then
        assertEquals(listOf("1.50", "10.99", "0.07", "2.35"), labels)
    }
}
