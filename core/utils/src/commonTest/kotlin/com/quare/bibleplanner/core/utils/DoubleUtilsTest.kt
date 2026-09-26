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

    @Test
    fun `GIVEN amounts whose cents are not exactly representable WHEN formatting as money THEN rounds the cents`() {
        // When
        val labels = listOf(4.05, 0.29, 1.15, 0.57, 8.2).map(Double::toMoneyFormat)

        // Then
        assertEquals(listOf("4.05", "0.29", "1.15", "0.57", "8.20"), labels)
    }

    @Test
    fun `GIVEN negative amounts WHEN formatting as money THEN keeps the sign`() {
        // When
        val labels = listOf(-0.5, -4.05, -3.0).map(Double::toMoneyFormat)

        // Then
        assertEquals(listOf("-0.50", "-4.05", "-3"), labels)
    }
}
